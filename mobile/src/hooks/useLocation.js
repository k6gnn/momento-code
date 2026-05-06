import { useEffect, useRef, useState } from 'react';
import * as Location from 'expo-location';

/**
 * Returns the device's current GPS coordinates and any permission/error state.
 *
 * - Requests foreground permission on first mount.
 * - Gets an immediate fix then watches for updates every 10 s / 10 m.
 * - Cleans up the watcher subscription on unmount so there are no leaks.
 */
export default function useLocation() {
  const [location, setLocation] = useState(null);
  const [error, setError] = useState(null);
  // Keep a ref so the cleanup callback always has the latest subscription
  // even if it was assigned asynchronously after the component unmounted.
  const subRef = useRef(null);

  useEffect(() => {
    let isMounted = true;

    (async () => {
      const { status } = await Location.requestForegroundPermissionsAsync();
      if (!isMounted) return;

      if (status !== 'granted') {
        setError('Location permission denied. Please enable it in Settings.');
        return;
      }

      try {
        const current = await Location.getCurrentPositionAsync({
          accuracy: Location.Accuracy.Balanced,
        });
        if (isMounted) setLocation(current.coords);

        const sub = await Location.watchPositionAsync(
          {
            accuracy: Location.Accuracy.Balanced,
            timeInterval: 10000,
            distanceInterval: 10,
          },
          next => {
            if (isMounted) setLocation(next.coords);
          },
        );
        // Store the subscription so we can clean it up later.
        subRef.current = sub;
      } catch (e) {
        if (isMounted) setError(`Could not get location: ${e.message}`);
      }
    })();

    return () => {
      isMounted = false;
      subRef.current?.remove();
      subRef.current = null;
    };
  }, []);

  return { location, error };
}
