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
          50: '#f0f9ff',
          900: '#162013',
        },
        secondary: {
          500: '#426039',
          600: '#2e4328',
        },
        accent: {
          400: '#a2c398',
          500: '#8cd279',
        },
        success: '#0bda35',
        error: '#fa4b38',
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

