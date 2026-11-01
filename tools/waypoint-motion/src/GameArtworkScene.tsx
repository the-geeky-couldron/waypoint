import React from 'react';
import {
  Img,
  Interactive,
  interpolate,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
  type InteractivitySchema,
} from 'remotion';

type GameArtworkSceneProps = {
  readonly title: string;
  readonly game: string;
  readonly artwork: string;
  readonly accentColor: string;
  readonly routeColor: string;
  readonly routeStyle: 'urban' | 'neon' | 'pixel' | 'trail' | 'coast';
  readonly style?: React.CSSProperties;
};

const SceneRoot: React.FC<GameArtworkSceneProps> = ({
  title,
  game,
  artwork,
  accentColor,
  routeColor,
  routeStyle,
  style,
}) => {
  const frame = useCurrentFrame();
  const {durationInFrames} = useVideoConfig();
  const entrance = interpolate(frame, [0, 18], [0.72, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  const exit = interpolate(frame, [durationInFrames - 18, durationInFrames], [1, 0.72], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  const reveal = interpolate(frame, [14, durationInFrames * 0.72], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  const zoom = interpolate(frame, [0, durationInFrames], [1.06, 1.17], {
    extrapolateRight: 'clamp',
  });
  const pan = interpolate(frame, [0, durationInFrames], [-0.9, 0.9], {
    extrapolateRight: 'clamp',
  });
  const pulse = 0.78 + Math.sin(frame * 0.095) * 0.12;
  const lineProgress = reveal * 1000;
  const route = routeDefinition(routeStyle);
  const routeSamples = sampleRoute(route.segments);
  const marker = routePosition(reveal, routeSamples);
  const lineCap = routeStyle === 'pixel' ? 'square' : 'round';
  const lineJoin = routeStyle === 'pixel' ? 'miter' : 'round';

  return (
    <Interactive.Div
      style={{
        position: 'absolute',
        inset: 0,
        overflow: 'hidden',
        backgroundColor: '#0E110F',
        opacity: entrance * exit,
        ...style,
      }}
      aria-label={`${game} map artwork, ${title} theme`}
    >
      <Img
        src={staticFile(artwork)}
        style={{
          position: 'absolute',
          inset: 0,
          width: '100%',
          height: '100%',
          objectFit: 'cover',
          transform: `translate(${pan}%, 0) scale(${zoom})`,
        }}
      />
      <div
        style={{
          position: 'absolute',
          inset: 0,
          background:
            'linear-gradient(180deg, rgba(8,10,9,.14) 4%, rgba(8,10,9,.06) 34%, rgba(8,10,9,.38) 59%, rgba(8,10,9,.96) 100%)',
        }}
      />
      <svg
        viewBox="0 0 720 1280"
        style={{position: 'absolute', inset: 0, width: '100%', height: '100%'}}
        aria-hidden="true"
      >
        <g fill="none" stroke={routeColor} strokeWidth="3" opacity=".42">
          {routeStyle === 'urban' && (
            <>
              <path d="M 0 300 H 720 M 0 430 H 720 M 0 560 H 720 M 0 690 H 720 M 0 820 H 720 M 0 950 H 720 M 110 260 V 990 M 245 260 V 990 M 380 260 V 990 M 515 260 V 990 M 650 260 V 990" />
              <path d="M 40 345 H 92 M 270 480 H 352 M 420 610 H 486 M 540 875 H 635" stroke={accentColor} strokeWidth="5" opacity=".5" />
            </>
          )}
          {routeStyle === 'neon' && (
            <>
              <path d="M 72 260 V 980 M 190 260 V 980 M 308 260 V 980 M 426 260 V 980 M 544 260 V 980 M 662 260 V 980 M 0 340 H 720 M 0 470 H 720 M 0 600 H 720 M 0 730 H 720 M 0 860 H 720" />
              <path d="M 90 390 H 170 V 460 H 280 M 340 580 H 420 V 510 H 600 M 120 800 H 255 V 730 H 390" stroke={accentColor} strokeWidth="4" />
              <rect x="220" y="505" width="58" height="54" rx="2" /><rect x="456" y="770" width="70" height="60" rx="2" />
            </>
          )}
          {routeStyle === 'pixel' && (
            <>
              <path d="M 0 300 H 720 M 0 420 H 720 M 0 540 H 720 M 0 660 H 720 M 0 780 H 720 M 0 900 H 720 M 90 250 V 1020 M 210 250 V 1020 M 330 250 V 1020 M 450 250 V 1020 M 570 250 V 1020 M 690 250 V 1020" strokeWidth="5" />
              <path d="M 118 332 H 176 V 390 H 118 Z M 367 452 H 427 V 512 H 367 Z M 510 694 H 568 V 752 H 510 Z M 244 820 H 304 V 880 H 244 Z" fill={routeColor} fillOpacity=".2" stroke={accentColor} strokeWidth="3" />
            </>
          )}
          {routeStyle === 'trail' && (
            <>
              <path d="M -40 400 C 110 260 188 540 330 404 S 540 310 760 450 M -30 520 C 100 380 230 660 360 524 S 570 425 760 565 M -40 690 C 130 540 210 820 390 680 S 585 575 760 710 M -40 850 C 115 700 230 970 410 842 S 590 740 760 885" />
              <path d="M 100 660 C 210 590 224 540 318 534 S 435 492 526 390" stroke={accentColor} strokeWidth="5" />
            </>
          )}
          {routeStyle === 'coast' && (
            <>
              <path d="M -20 390 C 110 330 122 500 245 450 S 382 330 480 406 S 610 535 750 435 M -20 510 C 105 452 140 620 260 570 S 390 452 500 530 S 625 660 750 560 M -20 800 C 115 736 150 900 275 845 S 405 735 515 812 S 630 930 750 840" />
              <path d="M 72 850 C 215 824 122 708 258 680 C 380 654 286 558 402 520 C 536 476 412 388 548 350" stroke={accentColor} strokeWidth="5" />
            </>
          )}
        </g>
        <path
          d={route.path}
          fill="none"
          stroke={routeColor}
          strokeWidth="20"
          strokeLinecap={lineCap}
          strokeLinejoin={lineJoin}
          opacity=".34"
        />
        <path
          d={route.path}
          pathLength="1000"
          fill="none"
          stroke={accentColor}
          strokeWidth="8"
          strokeLinecap={lineCap}
          strokeLinejoin={lineJoin}
          strokeDasharray="1000"
          strokeDashoffset={1000 - lineProgress}
        />
        <circle cx={route.start.x} cy={route.start.y} r="17" fill="#F1F2E8" />
        <circle cx={route.start.x} cy={route.start.y} r="8" fill="#101310" />
        <circle cx={marker.x} cy={marker.y} r={23 + pulse * 8} fill={accentColor} opacity=".18" />
        <circle cx={marker.x} cy={marker.y} r="12" fill={accentColor} />
        <circle cx={marker.x} cy={marker.y} r="5" fill="#101310" />
      </svg>
      <div
        style={{
          position: 'absolute',
          top: 92,
          left: 64,
          color: '#F1F2E8',
          fontFamily: 'Arial, sans-serif',
          fontSize: 22,
          fontWeight: 700,
          letterSpacing: 5,
        }}
      >
        WAYPOINT
      </div>
      <div
        style={{
          position: 'absolute',
          top: 132,
          left: 64,
          color: '#F1F2E8',
          fontFamily: 'Arial, sans-serif',
          fontSize: 28,
          fontWeight: 700,
          letterSpacing: 1.2,
          textShadow: '0 2px 18px rgba(0,0,0,.45)',
        }}
      >
        {title.toUpperCase()}
      </div>
    </Interactive.Div>
  );
};

type Point = {x: number; y: number};
type RouteSegment = {start: Point; end: Point; control1?: Point; control2?: Point};
type RouteSamples = {points: Point[]; distances: number[]; length: number};

function routeDefinition(style: GameArtworkSceneProps['routeStyle']) {
  if (style === 'neon') {
    const points = [
      {x: 88, y: 1050}, {x: 162, y: 975}, {x: 162, y: 884}, {x: 290, y: 884},
      {x: 290, y: 760}, {x: 402, y: 760}, {x: 402, y: 642}, {x: 526, y: 642},
      {x: 526, y: 512}, {x: 630, y: 512}, {x: 630, y: 340},
    ];
    return {
      path: 'M 88 1050 L 162 975 L 162 884 L 290 884 L 290 760 L 402 760 L 402 642 L 526 642 L 526 512 L 630 512 L 630 340',
      start: points[0],
      segments: lineSegments(points),
    };
  }
  if (style === 'pixel') {
    const points = [
      {x: 84, y: 1078}, {x: 222, y: 1078}, {x: 222, y: 970}, {x: 330, y: 970},
      {x: 330, y: 842}, {x: 456, y: 842}, {x: 456, y: 714}, {x: 552, y: 714},
      {x: 552, y: 588}, {x: 636, y: 588}, {x: 636, y: 438},
    ];
    return {
      path: 'M 84 1078 H 222 V 970 H 330 V 842 H 456 V 714 H 552 V 588 H 636 V 438',
      start: points[0],
      segments: lineSegments(points),
    };
  }
  if (style === 'trail') {
    const segments: RouteSegment[] = [
      {start: {x: 84, y: 1062}, control1: {x: 220, y: 1034}, control2: {x: 96, y: 904}, end: {x: 230, y: 872}},
      {start: {x: 230, y: 872}, control1: {x: 372, y: 837}, control2: {x: 188, y: 736}, end: {x: 331, y: 697}},
      {start: {x: 331, y: 697}, control1: {x: 454, y: 664}, control2: {x: 309, y: 556}, end: {x: 451, y: 518}},
      {start: {x: 451, y: 518}, control1: {x: 570, y: 486}, control2: {x: 491, y: 383}, end: {x: 638, y: 326}},
    ];
    return {
      path: 'M 84 1062 C 220 1034 96 904 230 872 C 372 837 188 736 331 697 C 454 664 309 556 451 518 C 570 486 491 383 638 326',
      start: segments[0].start,
      segments,
    };
  }
  if (style === 'coast') {
    const segments: RouteSegment[] = [
      {start: {x: 72, y: 1040}, control1: {x: 222, y: 1010}, control2: {x: 110, y: 866}, end: {x: 248, y: 824}},
      {start: {x: 248, y: 824}, control1: {x: 372, y: 786}, control2: {x: 264, y: 695}, end: {x: 384, y: 646}},
      {start: {x: 384, y: 646}, control1: {x: 514, y: 594}, control2: {x: 388, y: 482}, end: {x: 520, y: 438}},
      {start: {x: 520, y: 438}, control1: {x: 626, y: 402}, control2: {x: 536, y: 324}, end: {x: 650, y: 290}},
    ];
    return {
      path: 'M 72 1040 C 222 1010 110 866 248 824 C 372 786 264 695 384 646 C 514 594 388 482 520 438 C 626 402 536 324 650 290',
      start: segments[0].start,
      segments,
    };
  }
  const segments: RouteSegment[] = [
    {start: {x: 82, y: 1040}, control1: {x: 166, y: 988}, control2: {x: 186, y: 842}, end: {x: 269, y: 842}},
    {start: {x: 269, y: 842}, control1: {x: 358, y: 842}, control2: {x: 320, y: 732}, end: {x: 404, y: 707}},
    {start: {x: 404, y: 707}, control1: {x: 493, y: 680}, control2: {x: 431, y: 557}, end: {x: 522, y: 532}},
    {start: {x: 522, y: 532}, control1: {x: 585, y: 516}, control2: {x: 548, y: 399}, end: {x: 635, y: 328}},
  ];
  return {
    path: 'M 82 1040 C 166 988 186 842 269 842 C 358 842 320 732 404 707 C 493 680 431 557 522 532 C 585 516 548 399 635 328',
    start: segments[0].start,
    segments,
  };
}

function lineSegments(points: Point[]): RouteSegment[] {
  return points.slice(1).map((end, index) => ({start: points[index], end}));
}

function sampleRoute(segments: RouteSegment[]): RouteSamples {
  const points: Point[] = [];
  const distances: number[] = [];
  let length = 0;

  for (const segment of segments) {
    const isCurve = segment.control1 !== undefined && segment.control2 !== undefined;
    const steps = isCurve ? 64 : Math.max(1, Math.ceil(Math.hypot(segment.end.x - segment.start.x, segment.end.y - segment.start.y) / 18));
    for (let index = points.length === 0 ? 0 : 1; index <= steps; index++) {
      const t = index / steps;
      const point = isCurve
        ? cubicPoint(segment.start, segment.control1!, segment.control2!, segment.end, t)
        : {
            x: segment.start.x + (segment.end.x - segment.start.x) * t,
            y: segment.start.y + (segment.end.y - segment.start.y) * t,
          };
      if (points.length > 0) {
        const previous = points[points.length - 1];
        length += Math.hypot(point.x - previous.x, point.y - previous.y);
      }
      points.push(point);
      distances.push(length);
    }
  }
  return {points, distances, length};
}

function cubicPoint(start: Point, control1: Point, control2: Point, end: Point, t: number): Point {
  const inverse = 1 - t;
  return {
    x: inverse ** 3 * start.x + 3 * inverse ** 2 * t * control1.x + 3 * inverse * t ** 2 * control2.x + t ** 3 * end.x,
    y: inverse ** 3 * start.y + 3 * inverse ** 2 * t * control1.y + 3 * inverse * t ** 2 * control2.y + t ** 3 * end.y,
  };
}

function routePosition(progress: number, route: RouteSamples): Point {
  const target = Math.max(0, Math.min(1, progress)) * route.length;
  let low = 0;
  let high = route.distances.length - 1;
  while (low < high) {
    const middle = Math.floor((low + high) / 2);
    if (route.distances[middle] < target) low = middle + 1;
    else high = middle;
  }
  const endIndex = low;
  if (endIndex === 0) return route.points[0];
  const startIndex = endIndex - 1;
  const segmentLength = route.distances[endIndex] - route.distances[startIndex];
  const fraction = segmentLength === 0 ? 0 : (target - route.distances[startIndex]) / segmentLength;
  return {
    x: route.points[startIndex].x + (route.points[endIndex].x - route.points[startIndex].x) * fraction,
    y: route.points[startIndex].y + (route.points[endIndex].y - route.points[startIndex].y) * fraction,
  };
}

const sceneSchema = {
  title: {type: 'text-content', default: 'Los Santos', description: 'Map style name'},
  game: {type: 'text-content', default: 'Grand Theft Auto V', description: 'Game title'},
  artwork: {
    type: 'asset',
    assetType: 'image',
    default: 'game-art/art_los_santos.jpg',
    description: 'Official game artwork',
  },
  accentColor: {type: 'color', default: '#B6DB91', description: 'Waypoint accent'},
  routeColor: {type: 'color', default: '#B786ED', description: 'Route line'},
  routeStyle: {
    type: 'enum',
    default: 'urban',
    description: 'Route geometry',
    variants: {urban: {}, neon: {}, pixel: {}, trail: {}, coast: {}},
  },
} as const satisfies InteractivitySchema;

export const GameArtworkScene = Interactive.withSchema({
  Component: SceneRoot,
  componentName: '<GameArtworkScene>',
  schema: sceneSchema,
  wrapInSequence: true,
});
