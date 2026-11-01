import React from 'react';
import {AbsoluteFill} from 'remotion';
import {GameArtworkScene} from './GameArtworkScene';

/** A silent, looping art reel used as the first onboarding screen's moving backdrop. */
export const WaypointIntroVideo: React.FC = () => (
  <AbsoluteFill style={{backgroundColor: '#071326'}}>
    <GameArtworkScene
      name="Los Santos"
      from={0}
      durationInFrames={150}
      title="Los Santos"
      game="Grand Theft Auto V"
      artwork="game-art/art_los_santos.jpg"
      accentColor="#B6DB91"
      routeColor="#B786ED"
      routeStyle="urban"
    />
    <GameArtworkScene
      name="Night City"
      from={126}
      durationInFrames={150}
      title="Night City"
      game="Cyberpunk 2077"
      artwork="game-art/art_night_city.jpg"
      accentColor="#6DE9DD"
      routeColor="#F6E75C"
      routeStyle="neon"
    />
    <GameArtworkScene
      name="Pixel World"
      from={252}
      durationInFrames={150}
      title="Pixel World"
      game="Minecraft"
      artwork="game-art/art_pixel.png"
      accentColor="#B8E088"
      routeColor="#E95345"
      routeStyle="pixel"
    />
    <GameArtworkScene
      name="Hyrule"
      from={378}
      durationInFrames={150}
      title="Sheikah Slate"
      game="The Legend of Zelda: Breath of the Wild"
      artwork="game-art/art_hyrule.jpg"
      accentColor="#81D9FF"
      routeColor="#F0CE6B"
      routeStyle="trail"
    />
    <GameArtworkScene
      name="Vice Coast"
      from={504}
      durationInFrames={150}
      title="Vice Coast"
      game="Grand Theft Auto: Vice City"
      artwork="game-art/art_vice_coast.jpg"
      accentColor="#FFAFE1"
      routeColor="#F46AC0"
      routeStyle="coast"
    />
  </AbsoluteFill>
);
