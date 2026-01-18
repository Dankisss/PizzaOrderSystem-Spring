import { createSlice } from '@reduxjs/toolkit'

const initialState = {
  token: localStorage.getItem('token') || null,
  role: localStorage.getItem('role') || null,
  isAuthenticated: !!localStorage.getItem('token'),
  userId: localStorage.getItem('userId')
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setCredentials: (state, action) => {
      const { token, role, userId } = action.payload;
      state.token = token;
      state.role = role;
      state.isAuthenticated = true;
      state.userId = userId;
      localStorage.setItem('token', token);
      localStorage.setItem('role', role);
      localStorage.setItem('userId', userId)
    },
    logout: (state) => {
      state.token = null;
      state.role = null;
      state.isAuthenticated = false;
      state.userId = -1;
      localStorage.clear();
    },
  },
});

export const { setCredentials, logout } = authSlice.actions;
export default authSlice.reducer;