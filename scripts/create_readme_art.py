#!/usr/bin/env python3
"""Compose README art from the test's explicit London demo captures (requires Pillow)."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter
import argparse

parser=argparse.ArgumentParser()
parser.add_argument('captures',type=Path)
args=parser.parse_args()
out=Path('docs/images');out.mkdir(parents=True,exist_ok=True)
for name in ['los_santos','old_west','night_city','rivia','hyrule','pixel','welcome','library','updates']:
    capture = 'updates-settings' if name == 'updates' and (args.captures/'updates-settings.png').exists() else name
    source=Image.open(args.captures/f'{capture}.png').convert('RGB')
    # Remove the device status/navigation bars, including personal notification icons.
    source=source.crop((0,112,source.width,source.height-42))
    source.resize((540,round(source.height*540/source.width)),Image.Resampling.LANCZOS).save(out/f'{name.replace("_","-")}.jpg',quality=90,optimize=True)
W,H=1600,1120
canvas=Image.new('RGB',(W,H),'#151815')
d=ImageDraw.Draw(canvas)
fonts=Path('app/src/main/res/font')
def font(name,size):return ImageFont.truetype(str(fonts/name),size)
cream='#F1F2E8';accent='#D5D596';muted='#AFB5A7'
def centered(text,y,face,color):
    d.text(((W-d.textlength(text,font=face))/2,y),text,font=face,fill=color)
centered('WAYPOINT',48,font('barlow_condensed_bold.ttf',68),cream)
centered('REAL STREETS. GAME WORLDS.',151,font('barlow_condensed_bold.ttf',112),accent)
centered('Your everyday journey, through the worlds you love.',291,font('noto_sans.ttf',30),cream)
# A photographic product composition made only from real, anonymised demo captures.
for name,x,y,width in [('old-west',110,465,285),('rivia',1205,465,285),('night-city',883,405,310),('los-santos',407,370,370)]:
    shot=Image.open(out/f'{name}.jpg')
    height=round(shot.height*width/shot.width)
    shot=shot.resize((width,height),Image.Resampling.LANCZOS)
    shadow=Image.new('RGBA',(W,H));sd=ImageDraw.Draw(shadow)
    sd.rounded_rectangle((x-6,y+18,x+width+6,y+height+24),radius=24,fill=(0,0,0,145))
    shadow=shadow.filter(ImageFilter.GaussianBlur(22));canvas.paste(shadow,(0,0),shadow)
    mask=Image.new('L',shot.size);ImageDraw.Draw(mask).rounded_rectangle((0,0,width,height),radius=18,fill=255)
    canvas.paste(shot,(x,y),mask)
# End the image cleanly; the complete, uncropped screenshots remain in the gallery.
d=ImageDraw.Draw(canvas)
d.rectangle((0,1040,W,H),fill='#151815')
centered('27 WORLDS    ·    CAR / MOTORCYCLE / WALK    ·    EVERY WORLD INCLUDED',1068,font('barlow_condensed_bold.ttf',29),cream)
canvas.save(out/'hero.png',optimize=True)
thumb=Image.new('RGB',(1080,750),'#151815')
for i,name in enumerate(['los-santos','old-west','night-city','rivia','hyrule','pixel','welcome','library','updates']):
    im=Image.open(out/f'{name}.jpg');im.thumbnail((110,670))
    thumb.paste(im,(i*120,40));ImageDraw.Draw(thumb).text((i*120,10),name,fill='white')
thumb.save('.tooling/readme-contact.jpg')
print('README images generated from demonstration captures')
