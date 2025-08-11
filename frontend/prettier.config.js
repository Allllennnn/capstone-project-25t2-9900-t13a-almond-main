// prettier.config.js
module.exports = {
  // When encountering CRLF, automatically use the file's existing line ending instead of forcing LF
  endOfLine: 'auto',

  // The following are some optional common rules; you can add or remove them as needed for your project
  // Use single quotes
  singleQuote: true,
  // No semicolons at the end of statements
  semi: false,
  // Maximum line length
  printWidth: 100,
  // Number of spaces for indentation
  tabWidth: 2,
  // Omit quotes around object properties when possible
  quoteProps: 'as-needed',
  // Do not require parentheses for single arrow function parameter
  arrowParens: 'avoid'
}
