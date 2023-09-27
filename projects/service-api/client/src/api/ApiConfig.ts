import { AUTH_CONFIG } from 'src/auth/configuration';

interface ApiConfig {
  api_host: string;
}

export const API_CONFIG: ApiConfig = {
  api_host: AUTH_CONFIG.audience,
};
