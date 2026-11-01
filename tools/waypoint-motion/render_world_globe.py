import json
import math
import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
from shapely.affinity import translate
from shapely.geometry import box, shape, Polygon, MultiPolygon, GeometryCollection

ROOT = Path(__file__).resolve().parents[2]
DATA = Path(__file__).resolve().parent / 'data'
COUNTRIES = json.loads((DATA / 'countries.geojson').read_text())['features']
PLACES = json.loads((DATA / 'places.geojson').read_text())['features']
FONT = ROOT / 'app/src/main/res/font/barlow_condensed_bold.ttf'


def globe(center_lon=55, size=1024):
    r = size / 2 - 3
    cx = cy = size / 2
    image = Image.new('RGB', (size, size), '#020b05')
    draw = ImageDraw.Draw(image)
    draw.ellipse((cx-r, cy-r, cx+r, cy+r), fill='#092017', outline='#386d35', width=4)
    def point(lon, lat):
        phi = math.radians(lat)
        lam = math.radians(lon - center_lon)
        return (cx + r * math.cos(phi) * math.sin(lam), cy - r * math.sin(phi))

    def rings(geometry):
        if isinstance(geometry, Polygon):
            yield geometry
        elif isinstance(geometry, (MultiPolygon, GeometryCollection)):
            for item in geometry.geoms:
                yield from rings(item)

    def project_ring(coords):
        result = []
        for a, b in zip(coords, coords[1:]):
            lon1, lat1 = a[:2]
            lon2, lat2 = b[:2]
            steps = max(1, math.ceil(max(abs(lon2-lon1),abs(lat2-lat1))/2))
            for n in range(steps):
                t = n / steps
                result.append(point(lon1+(lon2-lon1)*t, lat1+(lat2-lat1)*t))
        return result

    clip = box(center_lon-89.95, -90, center_lon+89.95, 90)
    for feature in COUNTRIES:
        geometry = shape(feature['geometry'])
        for shift in (-360, 0, 360):
            moved = translate(geometry, xoff=shift)
            if not moved.intersects(clip):
                continue
            clipped = moved.intersection(clip)
            for polygon in rings(clipped):
                exterior = project_ring(list(polygon.exterior.coords))
                if len(exterior) > 2:
                    draw.polygon(exterior, fill='#26501e')
                    draw.line(exterior + [exterior[0]], fill='#537b37', width=2, joint='curve')
                for interior in polygon.interiors:
                    hole = project_ring(list(interior.coords))
                    if len(hole) > 2:
                        draw.polygon(hole, fill='#092017')

    # Directional light is calculated from the normal of the sphere. The rim and
    # terminator stay fixed while geography rotates across the lit surface.
    shade = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    pixels = shade.load()
    for y in range(size):
        for x in range(size):
            dist = math.hypot((x-cx)/r, (y-cy)/r)
            if dist > 1:
                continue
            normal_z = math.sqrt(max(0, 1 - dist * dist))
            normal_x = (x-cx)/r
            normal_y = (cy-y)/r
            light = max(0, normal_x * -.34 + normal_y * .39 + normal_z * .86)
            edge = max(0, (dist-.70)/.30)
            alpha = int(min(176, 72 * (1-light) + 105 * edge * edge + (7 if y % 5 == 0 else 0)))
            pixels[x,y] = (0, 4, 0, alpha)
    image = Image.alpha_composite(image.convert('RGBA'), shade)
    draw = ImageDraw.Draw(image)
    country_font = ImageFont.truetype(str(FONT), 20)
    city_font = ImageFont.truetype(str(FONT), 15)
    occupied = []

    def label(lon, lat, name, font, fill, min_margin=4):
        delta = (lon-center_lon+180)%360-180
        if abs(delta)>78:
            return
        x,y = point(center_lon+delta, lat)
        if math.hypot((x-cx)/r,(y-cy)/r)>.9:
            return
        name=name.upper()
        bb=draw.textbbox((0,0),name,font=font)
        w,h=bb[2]-bb[0],bb[3]-bb[1]
        rect=(x-w/2-min_margin,y-h/2-min_margin,x+w/2+min_margin,y+h/2+min_margin)
        if any(not (rect[2]<a or rect[0]>c or rect[3]<b or rect[1]>d) for a,b,c,d in occupied):
            return
        occupied.append(rect)
        draw.text((x-w/2,y-h/2),name,font=font,fill=fill,stroke_width=1,stroke_fill='#0b2209')

    label(16,78.2,'Svalbard',country_font,'#8aff38',9)
    featured_countries = {
        'Finland', 'Ukraine', 'Turkey', 'Kazakhstan', 'Uzbekistan',
        'Iran', 'Afghanistan', 'Pakistan', 'India', 'Saudi Arabia',
        'Mongolia', 'China', 'Egypt', 'Indonesia', 'Japan',
    }
    for f in sorted(COUNTRIES,key=lambda f:f['properties'].get('LABELRANK',9)):
        p=f['properties'];name=p.get('NAME_EN') or p['NAME']
        if name == "People's Republic of China": name = 'China'
        if name == 'Democratic Republic of the Congo': name = 'DR Congo'
        if (p.get('LABELRANK',9)<=2 or name in featured_countries) and name != 'Antarctica':
            label(p['LABEL_X'],p['LABEL_Y'],name,country_font,'#8aff38',9)
    featured_cities = {
        'Moscow', 'Kazan', 'Novosibirsk', 'Chelyabinsk', 'Lahore',
        'Karachi', 'Mumbai', 'New Delhi', 'Beijing', 'Tokyo',
        'Bengaluru', 'Shanghai', 'Tehran', 'Istanbul',
    }
    for f in sorted(PLACES,key=lambda f:f['properties'].get('SCALERANK',9)):
        p=f['properties']
        name = p.get('NAMEASCII') or p['NAME']
        if p.get('SCALERANK',9)>0 and name not in featured_cities:continue
        lon,lat=f['geometry']['coordinates']
        delta=(lon-center_lon+180)%360-180
        if abs(delta)>78:continue
        x,y=point(center_lon+delta,lat)
        if math.hypot((x-cx)/r,(y-cy)/r)>.9:continue
        draw.ellipse((x-4,y-4,x+4,y+4),fill='#92ff37')
        label(lon,lat-1.4,name,city_font,'#b5ff72',5)
    return image.convert('RGB')


if __name__=='__main__':
    parser=argparse.ArgumentParser(description='Render Waypoint onboarding globe frames from public-domain data.')
    parser.add_argument('--size',type=int,default=1024)
    parser.add_argument('--frames',type=int,default=361)
    parser.add_argument('--start-longitude',type=float,default=55)
    parser.add_argument('--end-longitude',type=float,default=145)
    parser.add_argument('--out-dir',type=Path,default=ROOT/'.local/world-globe-frames')
    args=parser.parse_args()
    args.out_dir.mkdir(parents=True,exist_ok=True)
    for index in range(args.frames):
        longitude=args.start_longitude+(args.end_longitude-args.start_longitude)*index/max(1,args.frames-1)
        frame=globe(longitude,args.size)
        frame.save(args.out_dir/f'frame_{index:04d}.png')
        if index==0:
            frame.save(ROOT/'app/src/main/res/drawable-nodpi/onboarding_world_globe.png')
        if index%30==0:
            print(f'frame {index+1}/{args.frames}',flush=True)
    for offset,index in enumerate(range(args.frames-2,0,-1),start=args.frames):
        link=args.out_dir/f'frame_{offset:04d}.png'
        link.symlink_to(f'frame_{index:04d}.png')
