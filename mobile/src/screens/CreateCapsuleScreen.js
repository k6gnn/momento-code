import React, { useState } from 'react';
import { Alert, Text } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import Screen from '../components/Screen';
import { Button, Input, Label } from '../components/FormControls';
import useLocation from '../hooks/useLocation';
import { getApps, getApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';

export default function CreateCapsuleScreen() {
  const { location, error: locationError } = useLocation();
  const [textContent, setTextContent] = useState('');
  const [photoAsset, setPhotoAsset] = useState(null);
  const [loading, setLoading] = useState(false);

  const pickPhoto = async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission required', 'Please allow access to your photo library in Settings.');
      return;
    }
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: 'images',
      quality: 0.5,
      allowsEditing: true,
      aspect: [4, 3],
    });
    if (!result.canceled && result.assets?.length > 0) {
      setPhotoAsset(result.assets[0]);
    }
  };

  const submit = async () => {
    if (!location) {
      Alert.alert('Location required', locationError || 'Waiting for GPS fix. Try again in a moment.');
      return;
    }
    if (!textContent.trim() && !photoAsset) {
      Alert.alert('Empty capsule', 'Add a message or photo before dropping.');
      return;
    }

    setLoading(true);
    try {
      // Get Firebase token
      let token = null;
      if (getApps().length > 0) {
        const currentUser = getAuth(getApp()).currentUser;
        if (currentUser) {
          token = await currentUser.getIdToken(false);
        }
      }

      const baseUrl = process.env.EXPO_PUBLIC_API_BASE_URL;

      const form = new FormData();
      form.append('latitude', String(location.latitude));
      form.append('longitude', String(location.longitude));
      if (textContent.trim()) {
        form.append('textContent', textContent.trim());
      }
      if (photoAsset) {
        const uri = photoAsset.uri;
        const fileName = uri.split('/').pop() || 'photo.jpg';
        const mimeType = photoAsset.mimeType || 'image/jpeg';
        form.append('photo', { uri, name: fileName, type: mimeType });
      }

      // Use fetch instead of Axios — Axios has known issues with
      // FormData multipart on React Native (boundary not set correctly).
      const response = await fetch(`${baseUrl}/capsules`, {
        method: 'POST',
        headers: {
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
          // Do NOT set Content-Type — fetch sets it automatically
          // with the correct multipart boundary.
        },
        body: form,
      });

      if (!response.ok) {
        const body = await response.json().catch(() => ({}));
        throw new Error(body.message || `Server error ${response.status}`);
      }

      setTextContent('');
      setPhotoAsset(null);
      Alert.alert('Capsule dropped! 📦', 'Your capsule was saved at your current location.');
    } catch (e) {
      Alert.alert('Create failed', e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Screen>
      <Text style={{ fontSize: 28, fontWeight: '700' }}>Drop a capsule</Text>
      {locationError ? (
        <Text style={{ color: '#a12c7b' }}>{locationError}</Text>
      ) : !location ? (
        <Text style={{ color: '#6b6963' }}>📍 Getting your location…</Text>
      ) : (
        <Text style={{ color: '#437a22' }}>
          📍 {location.latitude.toFixed(5)}, {location.longitude.toFixed(5)}
        </Text>
      )}
      <Label>Message</Label>
      <Input
        multiline
        numberOfLines={4}
        value={textContent}
        onChangeText={setTextContent}
        placeholder="Write something for the finder…"
      />
      <Button
        title={photoAsset ? '✅ Photo selected — tap to change' : 'Choose photo'}
        kind="secondary"
        onPress={pickPhoto}
        disabled={loading}
      />
      <Button title="Drop capsule" onPress={submit} loading={loading} />
    </Screen>
  );
}