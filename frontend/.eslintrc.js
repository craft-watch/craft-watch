module.exports = {
  "env": {
    "browser": true,
    "es2020": true
  },
  "extends": [
    "eslint:recommended",
    "plugin:react/recommended",
    "plugin:@typescript-eslint/recommended",
  ],
  "parser": "@typescript-eslint/parser",
  "parserOptions": {
    "ecmaFeatures": {
      "jsx": true
    },
    "ecmaVersion": 11,
    "project": "./tsconfig.json",
    "sourceType": "module"
  },
  "plugins": [
    "react",
    "@typescript-eslint"
  ],
  "rules": {
    "react/prop-types": "off",   // We're using Typescript, so this adds little value
    "eol-last": ["error"],
    "max-len": ["error", 120],
    "no-trailing-spaces": "error",
    "@typescript-eslint/quotes": ["error", "double"],
    "@typescript-eslint/indent": ["error", 2, { "flatTernaryExpressions": true }],
    "@typescript-eslint/semi": "error",
  }
};
