"""Cut individually movable Waypoint stickers from the generated source artwork.

The composition and motion of the stickers are authored in ReferenceOnboarding.kt.
"""

from collections import deque
from pathlib import Path

import numpy as np
from PIL import Image, ImageEnhance


ROOT = Path(__file__).resolve().parents[2]
SOURCE = Path(__file__).resolve().parent / "data/sticker-source.png"
TARGET = ROOT / "app/src/main/res/drawable-nodpi"

# Seeds select one contiguous sticker silhouette. Bounds separate art that touches a neighbor.
STICKERS = {
    "pizza": ((150, 150), (20, 15, 340, 354)),
    "beer": ((450, 150), (340, 25, 593, 351)),
    "martini": ((680, 150), (565, 20, 780, 335)),
    "coffee": ((900, 170), (740, 5, 1005, 320)),
    "house": ((150, 400), (5, 260, 303, 565)),
    "car": ((450, 450), (300, 350, 602, 581)),
    "compass": ((690, 450), (590, 335, 795, 587)),
    "diamond": ((910, 450), (780, 330, 1018, 554)),
    "shield": ((150, 680), (25, 553, 299, 790)),
    "chest": ((430, 680), (274, 560, 565, 808)),
    "bed": ((700, 710), (563, 578, 841, 842)),
    "sword": ((920, 700), (828, 504, 1006, 891)),
    "pump": ((150, 920), (25, 785, 303, 1028)),
    "cross": ((400, 900), (287, 788, 503, 1008)),
    "controller": ((650, 960), (485, 845, 818, 1090)),
    "boot": ((930, 1000), (823, 886, 982, 1142)),
    "pin": ((120, 1150), (24, 1028, 226, 1268)),
    "fire": ((350, 1150), (223, 985, 471, 1244)),
}


def connected_pixels(allowed: np.ndarray, seed: tuple[int, int]) -> np.ndarray:
    height, width = allowed.shape
    x, y = seed
    if not allowed[y, x]:
        raise ValueError(f"Sticker seed {seed} is on the background")
    seen = np.zeros(allowed.shape, dtype=bool)
    seen[y, x] = True
    queue = deque([(x, y)])
    while queue:
        x, y = queue.popleft()
        for nx, ny in (
            (x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1),
            (x + 1, y + 1), (x - 1, y - 1), (x - 1, y + 1), (x + 1, y - 1),
        ):
            if 0 <= nx < width and 0 <= ny < height and allowed[ny, nx] and not seen[ny, nx]:
                seen[ny, nx] = True
                queue.append((nx, ny))
    return seen


def main() -> None:
    rgb = np.asarray(Image.open(SOURCE).convert("RGB"))
    height, width = rgb.shape[:2]
    red, green, blue = [rgb[:, :, index].astype("int16") for index in range(3)]
    blue_backdrop = ((blue > red + 12) & (blue > green + 9) & (blue > 25)) | (
        (red < 21) & (green < 21) & (blue < 21)
    )
    edge = np.zeros((height, width), dtype=bool)
    edge[0, :] = edge[-1, :] = edge[:, 0] = edge[:, -1] = True
    background = np.zeros((height, width), dtype=bool)
    for y, x in zip(*np.where(edge & blue_backdrop)):
        if not background[y, x]:
            background |= connected_pixels(blue_backdrop, (int(x), int(y)))
    foreground = ~background

    TARGET.mkdir(parents=True, exist_ok=True)
    for name, (seed, bounds) in STICKERS.items():
        component = connected_pixels(foreground, seed)
        left, top, right, bottom = bounds
        ys, xs = np.where(component[top:bottom, left:right])
        if not len(xs):
            raise ValueError(f"No pixels for {name}")
        left += max(0, int(xs.min()) - 4)
        top += max(0, int(ys.min()) - 4)
        right = min(width, left + int(xs.max() - xs.min()) + 9)
        bottom = min(height, top + int(ys.max() - ys.min()) + 9)
        rgba = np.dstack((rgb[top:bottom, left:right],
                          component[top:bottom, left:right].astype("uint8") * 255))
        sticker = Image.fromarray(rgba, "RGBA")
        # A short, hand-picked palette gives the source a print-like game icon
        # finish instead of the smooth highlights of the generated painting.
        white = Image.new("RGB", sticker.size, "#ffffff")
        white.paste(sticker, mask=sticker.getchannel("A"))
        white = ImageEnhance.Color(white).enhance(1.5)
        white = ImageEnhance.Contrast(white).enhance(1.35)
        ink = white.quantize(colors=12, method=Image.Quantize.MEDIANCUT).convert("RGBA")
        ink.putalpha(sticker.getchannel("A"))
        ink.save(
            TARGET / f"onboarding_sticker_{name}.webp", "WEBP", quality=90, method=6
        )


if __name__ == "__main__":
    main()
