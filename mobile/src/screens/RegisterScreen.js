import React, { useState } from 'react';
import { Alert, Text } from 'react-native';
import Screen from '../components/Screen';
import { Button, Input, Label } from '../components/FormControls';
import { useAuth } from '../context/AuthContext';

export default function RegisterScreen({ navigation }) {
  const { register } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    if (!email.trim() || !password) {
      Alert.alert('Missing fields', 'Please fill in all fields.');
      return;
    }
    if (password.length < 6) {
      Alert.alert('Weak password', 'Password must be at least 6 characters.');
      return;
    }
    setLoading(true);
    try {
      await register(email.trim(), password);
      // Navigation handled automatically by onAuthStateChanged in AppRouter.
    } catch (e) {
      Alert.alert('Registration failed', e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Screen>
      <Text style={{ fontSize: 28, fontWeight: '700' }}>Create account</Text>
      <Label>Email</Label>
      <Input
        autoCapitalize="none"
        keyboardType="email-address"
        textContentType="emailAddress"
        autoComplete="email"
        value={email}
        onChangeText={setEmail}
        placeholder="you@example.com"
      />
      <Label>Password</Label>
      <Input
        secureTextEntry
        textContentType="newPassword"
        autoComplete="new-password"
        value={password}
        onChangeText={setPassword}
        placeholder="Min 6 characters"
      />
      <Button title="Register" onPress={submit} loading={loading} />
      <Button
        title="Back to sign in"
        kind="secondary"
        onPress={() => navigation.goBack()}
        disabled={loading}
      />
    </Screen>
  );
}
