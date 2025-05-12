import Keycloak from 'keycloak-js';

// Initialize Keycloak instance
const keycloakConfig = {
  url: '/auth', // Proxied Keycloak server URL
  realm: 'camunda-platform', // Your realm
  clientId: 'camunda-app' // Your client ID
  // Note: clientSecret is not needed here for the JS adapter
};

const keycloak = new Keycloak(keycloakConfig);

// Function to initialize Keycloak
export const initKeycloak = () => {
  return new Promise((resolve, reject) => {
    try {
      console.log('Initializing Keycloak with config:', JSON.stringify(keycloakConfig));
      
      keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        pkceMethod: 'S256',
        enableLogging: true, // Enable logging for debugging
        checkLoginIframe: false, // Disable iframe checking which can cause issues
        flow: 'standard' // Use standard flow instead of implicit
      })
        .then(authenticated => {
          console.log('Keycloak initialized successfully, authenticated:', authenticated);
          resolve(authenticated);
        })
        .catch(error => {
          console.error('Failed to initialize Keycloak:', error);
          // Resolve with false instead of rejecting to prevent cascading errors
          resolve(false);
        });
    } catch (error) {
      console.error('Exception during Keycloak initialization:', error);
      // Resolve with false instead of rejecting to prevent cascading errors
      resolve(false);
    }
  });
};

// Function to login
export const login = () => {
  keycloak.login();
};

// Function to logout
export const logout = () => {
  keycloak.logout();
};

// Function to get token
export const getToken = () => {
  return keycloak.token;
};

// Function to update token
export const updateToken = (minValidity = 5) => {
  return new Promise((resolve, reject) => {
    keycloak.updateToken(minValidity)
      .then(refreshed => {
        if (refreshed) {
          console.log('Token refreshed');
        } else {
          console.log('Token not refreshed, valid for ' + 
            Math.round(keycloak.tokenParsed.exp + keycloak.timeSkew - new Date().getTime() / 1000) + ' seconds');
        }
        resolve(keycloak.token);
      })
      .catch(error => {
        console.error('Failed to refresh token:', error);
        reject(error);
      });
  });
};

// Function to check if user has a specific role
export const hasRole = (role) => {
  return keycloak.hasRealmRole(role);
};

// Function to get user info
export const getUserInfo = () => {
  if (keycloak.tokenParsed) {
    return {
      username: keycloak.tokenParsed.preferred_username,
      roles: keycloak.realmAccess?.roles || [],
      isAdmin: keycloak.hasRealmRole('admin'),
      isUser: keycloak.hasRealmRole('user')
    };
  }
  return null;
};

export default keycloak;