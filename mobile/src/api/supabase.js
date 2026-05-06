import 'react-native-url-polyfill/auto';
import { createClient } from '@supabase/supabase-js';
import * as FileSystem from 'expo-file-system/legacy';
import { decode } from 'base64-arraybuffer';

const supabaseUrl = process.env.EXPO_PUBLIC_SUPABASE_URL;
const supabaseAnonKey = process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY;

console.log('Connecting to Supabase:', supabaseUrl);

export const supabase = createClient(supabaseUrl, supabaseAnonKey);

export const uploadImage = async (uri, fileName) => {
  try {
    console.log('Reading file:', uri);
    const base64 = await FileSystem.readAsStringAsync(uri, {
      encoding: 'base64',
    });

    const fileExt = fileName.split('.').pop();
    const path = `capsules/${Date.now()}.${fileExt}`;

    console.log('Uploading to path:', path);
    const { data, error } = await supabase.storage
      .from('capsule-media')
      .upload(path, decode(base64), {
        contentType: 'image/jpeg',
        upsert: false,
      });

    if (error) {
      console.error('Supabase error detail:', error);
      throw error;
    }
    
    return data.path;
  } catch (error) {
    console.error('Supabase upload error:', error);
    throw error;
  }
};
