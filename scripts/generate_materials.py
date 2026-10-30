"""Original tileable material sprites. No game imagery is embedded in the APK."""
from PIL import Image,ImageDraw,ImageFilter
from pathlib import Path
import random, math
root=Path('app/src/main/res/drawable-nodpi');root.mkdir(exist_ok=True)
rng=random.Random(721)
# Explicit 16-pixel material scale, nearest-neighbour enlargement only.
for name,base in [('pixel_grass',(98,130,61)),('pixel_stone',(123,124,118)),('pixel_water',(55,96,152))]:
 im=Image.new('RGB',(16,16));p=im.load()
 for y in range(16):
  for x in range(16):
   n=rng.choice([-18,-9,0,0,7,13]);p[x,y]=tuple(max(0,min(255,v+n)) for v in base)
 if name=='pixel_stone':
  d=ImageDraw.Draw(im)
  for y in (0,5,10,15):d.line((0,y,15,y),fill=(73,75,72))
  for x,y in [(0,0),(8,0),(4,5),(12,5),(0,10),(8,10)]:d.line((x,y,x,y+4),fill=(81,83,79))
 im.resize((128,128),Image.Resampling.NEAREST).save(root/(name+'.png'))
# Paper fibre: tileable low-frequency pulp variation and subtle fine grain.
size=256
im=Image.new('RGBA',(size,size));p=im.load()
for y in range(size):
 for x in range(size):
  v=math.sin(x*math.pi/32)*math.sin(y*math.pi/64)*3+rng.gauss(0,3)
  p[x,y]=(61,43,21,max(0,min(24,int(7+v))))
im.save(root/'paper_fibre.png')
# Original pixel navigation sprites, on a coherent 16x16 grid.
sprites={
'search':[(4,3,9,3),(3,4,3,9),(10,4,10,9),(4,10,9,10),(10,10,11,11),(12,12,13,13)],
'locate':[(7,1,8,3),(7,12,8,14),(1,7,3,8),(12,7,14,8),(4,4,11,4),(4,11,11,11),(4,4,4,11),(11,4,11,11),(7,7,8,8)],
'huds':[(2,2,6,6),(9,2,13,6),(2,9,6,13),(9,9,13,13)],
'saved':[(4,2,11,11),(4,12,5,13),(10,12,11,13)],
'settings':[(6,2,9,3),(6,12,9,13),(2,6,3,9),(12,6,13,9),(4,4,11,11)],
'food':[(3,2,3,7),(5,2,5,13),(7,2,7,7),(3,7,7,8),(11,2,12,13)],
'place':[(7,2,8,2),(5,3,10,3),(3,4,12,4),(2,5,13,6),(4,7,11,13)],
'stay':[(2,4,3,13),(4,7,13,11),(12,7,13,13),(4,5,7,6)]}
for name,rects in sprites.items():
 im=Image.new('RGBA',(16,16));d=ImageDraw.Draw(im)
 for box in rects:d.rectangle(box,fill=(238,237,218,255))
 if name=='settings':d.rectangle((6,6,9,9),fill=(48,49,46,255))
 im.resize((64,64),Image.Resampling.NEAREST).save(root/('pixel_'+name+'.png'))
