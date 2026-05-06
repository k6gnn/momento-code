import React, { useState } from 'react';
import { Alert, Text } from 'react-native';
import Screen from '../components/Screen';
import { Button, Input, Label } from '../components/FormControls';
import { useAuth } from '../context/AuthContext';

export default function LoginScreen({ navigation }) {
  const { login } = useAuth();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const submit = async () => {
    try {
      await login(email.trim(), password);
    } catch (e) {
      Alert.alert('Login failed', e.message);
    }
  };

  return (
    <Screen>
      <Text style={{ fontSize: 32, fontWeight: '700' }}>Momento</Text>
      <Text>Sign in to discover and drop capsules.</Text>
      <Label>Email</Label>
      <Input autoCapitalize="none" keyboardType="email-address" value={email} onChangeText={setEmail} />
      <Label>Password</Label>
      <Input secureTextEntry value={password} onChangeText={setPassword} />
      <Button title="Sign in" onPress={submit} />
      <Button title="Create account" kind="secondary" onPress={() => navigation.navigate('Register')} />
    </Screen>
  );
}
