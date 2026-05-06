import React, { useState } from 'react';
import { Alert, Text } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import Screen from '../components/Screen';
import { Button, Input, Label } from '../components/FormControls';
import useLocation from '../hooks/useLocation';
import { createCapsule } from '../api/capsules';
import { uploadImage } from '../api/supabase';

export default function CreateCapsuleScreen() {
  const { location, error } = useLocation();
  const [textContent, setTextContent] = useState('');
  const [photoUri, setPhotoUri] = useState(null);
  const [isUploading, setIsUploading] = useState(false);

  const pickPhoto = async () => {
    const mediaTypes = ImagePicker.MediaType
      ? ImagePicker.MediaType.images
      : ImagePicker.MediaTypeOptions.Images;
    const result = await ImagePicker.launchImageLibraryAsync({ mediaTypes, quality: 0.7 });
    if (!result.canceled) setPhotoUri(result.assets[0].uri);
  };

  const submit = async () => {
    if (!location) return Alert.alert('Location required', 'Wait until GPS is available.');
    if (isUploading) return;

    setIsUploading(true);
    try {
      let storageKey = null;
      if (photoUri) {
        storageKey = await uploadImage(photoUri, 'photo.jpg');
      }

      await createCapsule({
        latitude: location.latitude,
        longitude: location.longitude,
        textContent,
        media: storageKey ? [{ 
          mediaType: 'PHOTO', 
          fileName: 'photo.jpg', 
          mimeType: 'image/jpeg',
          localUri: storageKey // We'll send the storage key in the localUri field for now
        }] : []
      });

      setTextContent('');
      setPhotoUri(null);
      Alert.alert('Capsule created', 'Your capsule was saved.');
    } catch (e) {
      Alert.alert('Create failed', e.response?.data?.message || e.message);
    } finally {
      setIsUploading(false);
    }
  };

  return (
    <Screen>
      <Text style={{ fontSize: 28, fontWeight: '700' }}>Drop a capsule</Text>
      <Label>Message</Label>
      <Input multiline numberOfLines={4} value={textContent} onChangeText={setTextContent} />
      
      {!location && !error && <Text style={{ color: '#666', fontSize: 12, marginBottom: 10 }}>📍 Searching for GPS...</Text>}
      {error && <Text style={{ color: 'red', fontSize: 12, marginBottom: 10 }}>❌ {error}</Text>}
      {location && <Text style={{ color: 'green', fontSize: 12, marginBottom: 10 }}>✅ Location fixed</Text>}

      <Button title={photoUri ? 'Photo selected' : 'Choose photo'} kind="secondary" onPress={pickPhoto} disabled={isUploading} />
      <Button title={isUploading ? 'Uploading...' : 'Drop capsule'} onPress={submit} disabled={isUploading || !location} />
    </Screen>
  );
}
