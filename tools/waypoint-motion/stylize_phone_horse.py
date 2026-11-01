"""Give the original Waypoint horse illustration a restrained western game-art grade."""

from pathlib import Path

import numpy as np
from PIL import Image, ImageEnhance, ImageFilter


ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / 'output/imagegen/waypoint-phone-horse.png'
TARGET = ROOT / 'app/src/main/res/drawable-nodpi/onboarding_phone_horse.webp'

image = Image.open(SOURCE).convert('RGB')
pixels = np.asarray(image).astype(np.float32)
light = np.clip((pixels.mean(axis=2) - 45) / 175, 0, 1)[..., None]
shadow = np.array([.89, .98, 1.09], dtype=np.float32)
highlight = np.array([1.13, 1.04, .90], dtype=np.float32)
pixels *= shadow * (1 - light) + highlight * light
# The palette step and small fixed grain read as print rather than a soft render.
pixels = np.floor(np.clip(pixels, 0, 255) / 9) * 9 + 4
grain = np.random.default_rng(171).normal(0, 2.0, (*pixels.shape[:2], 1))
pixels = np.clip(pixels + grain, 0, 255).astype('uint8')
graded = Image.fromarray(pixels)
graded = ImageEnhance.Contrast(graded).enhance(1.08)
graded = graded.filter(ImageFilter.UnsharpMask(radius=1.2, percent=50, threshold=5))
graded.save(TARGET, 'WEBP', quality=86, method=6)
