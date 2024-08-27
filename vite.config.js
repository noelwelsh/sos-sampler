import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

export default defineConfig({
  base: "./", // Use ./ to ensure relative paths
  plugins: [scalaJSPlugin()],
});
