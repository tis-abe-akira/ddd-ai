/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f8fafc',
          900: '#1e1e1e', // IntelliJ Dark background
        },
        secondary: {
          500: '#3c3c3c', // IntelliJ border/divider
          600: '#2b2b2b', // IntelliJ sidebar/panel
        },
        accent: {
          400: '#9aa7b0', // IntelliJ muted text
          500: '#6897bb', // IntelliJ blue accent
        },
        success: '#629755', // IntelliJ green
        error: '#bc3f3c',   // IntelliJ red
      },
      fontFamily: {
        'manrope': ['Manrope', 'Noto Sans', 'sans-serif'],
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
  ],
}

