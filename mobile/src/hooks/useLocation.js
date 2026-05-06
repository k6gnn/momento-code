import { useEffect, useState } from 'react';
import * as Location from 'expo-location';

export default function useLocation() {
  const [location, setLocation] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    let sub;
    (async () => {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (status !== 'granted') {
        setError('Location permission denied');
        return;
      }
      
      // Try to get last known position first for speed
      const lastKnown = await Location.getLastKnownPositionAsync();
      if (lastKnown) setLocation(lastKnown.coords);

      const current = await Location.getCurrentPositionAsync({ 
        accuracy: Location.Accuracy.Balanced,
        timeout: 5000 // 5 second timeout
      }).catch(() => null);
      
      if (current) setLocation(current.coords);

      sub = await Location.watchPositionAsync(
        { accuracy: Location.Accuracy.Balanced, timeInterval: 5000, distanceInterval: 5 },
        next => setLocation(next.coords)
      );
    })();
    return () => sub?.remove?.();
  }, []);

  return { location, error };
}
