import nodeResolve from '@rollup/plugin-node-resolve';
import commonjs from '@rollup/plugin-commonjs';

export default {
  input: 'src/index.js',
  output: [
    {
      file: 'dist/plugin.js',
      format: 'cjs',
      sourcemap: true,
      exports: 'named'
    },
    {
      file: 'dist/esm/index.js',
      format: 'es',
      sourcemap: true
    }
  ],
  external: ['@capacitor/core'],
  plugins: [
    nodeResolve(),
    commonjs()
  ],
  // Add this line to fix the error:
  inlineDynamicImports: true
}