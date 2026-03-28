import CustomPaletteProvider from './CustomPaletteProvider'
import CustomContextPadProvider from './CustomContextPadProvider'

export default {
  __init__: ['customPaletteProvider', 'customContextPadProvider'],
  customPaletteProvider: ['type', CustomPaletteProvider],
  customContextPadProvider: ['type', CustomContextPadProvider]
}
