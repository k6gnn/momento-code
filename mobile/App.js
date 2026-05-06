import React from 'react';
import { NavigationContainer, DefaultTheme } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { StatusBar } from 'expo-status-bar';
import { AuthProvider, useAuth } from './src/context/AuthContext';
import LoginScreen from './src/screens/LoginScreen';
import RegisterScreen from './src/screens/RegisterScreen';
import MapScreen from './src/screens/MapScreen';
import CreateCapsuleScreen from './src/screens/CreateCapsuleScreen';
import ProfileScreen from './src/screens/ProfileScreen';
import HistoryScreen from './src/screens/HistoryScreen';
import DroppedCapsulesScreen from './src/screens/DroppedCapsulesScreen';
import CapsuleDetailScreen from './src/screens/CapsuleDetailScreen';
import { palette } from './src/theme/palette';

const RootStack = createNativeStackNavigator();
const Tab = createBottomTabNavigator();
const AuthStack = createNativeStackNavigator();
const MapStack = createNativeStackNavigator();

function MapStackNavigator() {
  return (
    <MapStack.Navigator>
      <MapStack.Screen name="MapHome" component={MapScreen} options={{ title: 'Nearby Capsules' }} />
      <MapStack.Screen name="CapsuleDetail" component={CapsuleDetailScreen} options={{ title: 'Capsule' }} />
    </MapStack.Navigator>
  );
}

function AppTabs() {
  return (
    <Tab.Navigator screenOptions={{ headerShown: false }}>
      <Tab.Screen name="Map" component={MapStackNavigator} />
      <Tab.Screen name="Create" component={CreateCapsuleScreen} />
      <Tab.Screen name="History" component={HistoryScreen} />
      <Tab.Screen name="Dropped" component={DroppedCapsulesScreen} />
      <Tab.Screen name="Profile" component={ProfileScreen} />
    </Tab.Navigator>
  );
}

function AuthNavigator() {
  return (
    <AuthStack.Navigator>
      <AuthStack.Screen name="Login" component={LoginScreen} />
      <AuthStack.Screen name="Register" component={RegisterScreen} />
    </AuthStack.Navigator>
  );
}

function AppRouter() {
  const { user, initializing } = useAuth();
  if (initializing) return null;
  return (
    <NavigationContainer theme={{ ...DefaultTheme, colors: { ...DefaultTheme.colors, background: palette.bg, text: palette.text, card: palette.surface, border: palette.border, primary: palette.primary } }}>
      <RootStack.Navigator screenOptions={{ headerShown: false }}>
        {user ? <RootStack.Screen name="AppTabs" component={AppTabs} /> : <RootStack.Screen name="Auth" component={AuthNavigator} />}
      </RootStack.Navigator>
      <StatusBar style="dark" />
    </NavigationContainer>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRouter />
    </AuthProvider>
  );
}
