import React, { useState } from 'react';
import { Alert, Text } from 'react-native';
import Screen from '../components/Screen';
import { Button, Input, Label } from '../components/FormControls';
import { useAuth } from '../context/AuthContext';

export default function LoginScreen({ navigation }) {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async () => {
    if (!email.trim() || !password) {
      Alert.alert('Missing fields', 'Please enter your email and password.');
      return;
    }
    setLoading(true);
    try {
      await login(email.trim(), password);
      // Navigation is handled automatically by AppRouter once user state changes.
    } catch (e) {
      Alert.alert('Login failed', e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Screen>
      <Text style={{ fontSize: 32, fontWeight: '700' }}>Momento</Text>
      <Text>Sign in to discover and drop capsules.</Text>
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
        textContentType="password"
        autoComplete="password"
        value={password}
        onChangeText={setPassword}
        placeholder="••••••••"
      />
      <Button title="Sign in" onPress={submit} loading={loading} />
      <Button
        title="Create account"
        kind="secondary"
        onPress={() => navigation.navigate('Register')}
        disabled={loading}
      />
    </Screen>
  );
}
