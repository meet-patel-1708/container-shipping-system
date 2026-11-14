import { PortOption } from './app/features/fleet/fleet.model';

export type LatLng = [number, number];

/** Well-known Indian ports, used when a port has no latitude/longitude saved in Fleet. */
const KNOWN: { keys: string[]; at: LatLng }[] = [
  { keys: ['mumbai', 'inbom'], at: [18.95, 72.84] },
  { keys: ['jnpt', 'nhava', 'jawaharlal', 'nhava sheva'], at: [18.95, 72.95] },
  { keys: ['mundra', 'inmun'], at: [22.74, 69.70] },
  { keys: ['kandla', 'deendayal', 'inixy'], at: [23.00, 70.22] },
  { keys: ['pipavav'], at: [20.92, 71.51] },
  { keys: ['hazira'], at: [21.10, 72.64] },
  { keys: ['mormugao', 'goa'], at: [15.41, 73.80] },
  { keys: ['new mangalore', 'mangalore'], at: [12.93, 74.81] },
  { keys: ['kochi', 'cochin', 'vallarpadam'], at: [9.97, 76.27] },
  { keys: ['tuticorin', 'thoothukudi', 'v.o. chidambaranar'], at: [8.76, 78.18] },
  { keys: ['chennai', 'madras', 'kattupalli', 'ennore'], at: [13.09, 80.30] },
  { keys: ['visakhapatnam', 'vizag'], at: [17.69, 83.30] },
  { keys: ['kakinada'], at: [16.95, 82.28] },
  { keys: ['paradip'], at: [20.26, 86.67] },
  { keys: ['haldia'], at: [22.03, 88.07] },
  { keys: ['kolkata', 'calcutta'], at: [22.55, 88.31] },
];

/** Coordinates from Fleet if present, otherwise a lookup by port name/code, otherwise null. */
export function portLatLng(p?: PortOption | null): LatLng | null {
  if (!p) return null;
  if (p.latitude != null && p.longitude != null) return [Number(p.latitude), Number(p.longitude)];
  const text = `${p.portName ?? ''} ${p.portCode ?? ''}`.toLowerCase();
  return KNOWN.find((k) => k.keys.some((w) => text.includes(w)))?.at ?? null;
}

/** Straight route (the dummy ship is not meant to be a real sea-lane). */
export function routePoints(from: LatLng, to: LatLng, steps: number): LatLng[] {
  return Array.from({ length: steps + 1 }, (_, i) => {
    const t = i / steps;
    return [from[0] + (to[0] - from[0]) * t, from[1] + (to[1] - from[1]) * t] as LatLng;
  });
}