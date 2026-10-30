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
W,H=1800,980
canvas=Image.new('RGB',(W,H),'#151815')
d=ImageDraw.Draw(canvas)
fonts=Path('app/src/main/res/font')
def font(name,size):return ImageFont.truetype(str(fonts/name),size)
cream='#F1F2E8';accent='#D5D596';muted='#AFB5A7'
d.polygon([(86,124),(125,53),(115,99)],fill=accent)
d.text((150,50),'WAYPOINT',font=font('barlow_condensed_bold.ttf',70),fill=cream)
d.text((80,265),'REAL STREETS.',font=font('barlow_condensed_bold.ttf',114),fill=cream)
d.text((80,385),'GAME WORLDS.',font=font('barlow_condensed_bold.ttf',114),fill=accent)
d.text((84,557),'Your everyday journey,',font=font('noto_sans.ttf',32),fill=muted)
d.text((84,605),'through the worlds you love.',font=font('noto_sans.ttf',32),fill=muted)
d.line((84,716,692,716),fill='#41483D',width=2)
d.text((84,749),'27 WORLDS  /  NATIVE ANDROID',font=font('barlow_condensed_bold.ttf',33),fill=cream)
d.text((84,808),'Car · Motorcycle · Walk',font=font('noto_sans.ttf',26),fill=muted)
d.text((84,911),'OPEN SOURCE. EVERY WORLD INCLUDED.',font=font('barlow_condensed_bold.ttf',25),fill=muted)
for name,x,y,width in [('old-west',802,197,292),('night-city',1396,197,292),('los-santos',1088,100,330)]:
    shot=Image.open(out/f'{name}.jpg')
    height=round(shot.height*width/shot.width)
    shot=shot.resize((width,height),Image.Resampling.LANCZOS)
    shadow=Image.new('RGBA',(W,H));sd=ImageDraw.Draw(shadow)
    sd.rounded_rectangle((x-10,y+10,x+width+10,y+height+26),radius=32,fill=(0,0,0,120))
    shadow=shadow.filter(ImageFilter.GaussianBlur(20));canvas.paste(shadow,(0,0),shadow)
    d=ImageDraw.Draw(canvas);d.rounded_rectangle((x-7,y-7,x+width+7,y+height+7),radius=25,fill='#41483D')
    mask=Image.new('L',shot.size);ImageDraw.Draw(mask).rounded_rectangle((0,0,width,height),radius=19,fill=255)
    canvas.paste(shot,(x,y),mask)
canvas.save(out/'hero.png',optimize=True)
thumb=Image.new('RGB',(1080,750),'#151815')
for i,name in enumerate(['los-santos','old-west','night-city','rivia','hyrule','pixel','welcome','library','updates']):
    im=Image.open(out/f'{name}.jpg');im.thumbnail((110,670))
    thumb.paste(im,(i*120,40));ImageDraw.Draw(thumb).text((i*120,10),name,fill='white')
thumb.save('.tooling/readme-contact.jpg')
print('README images generated from demonstration captures')
