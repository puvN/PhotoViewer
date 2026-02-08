# PhotoViewer

A cross-platform image viewer and editor built with Java and JavaFX, featuring AI-powered assistance for image manipulation.

## Features

- **Image Viewing**: Open and view PNG, JPG, GIF, BMP, and WebP images
- **Zoom & Pan**: Smooth zoom in/out and pan controls
- **Selection Tool**: Create rectangular selections and crop images
- **Scissors Tool**: Select, move, and paste image regions anywhere
- **Pencil Tool**: Draw freehand with customizable colors and brush sizes
- **Undo/Redo**: Full undo/redo support for all operations
- **AI Integration**: Chat with AI models (OpenAI, Anthropic, Google Gemini) to analyze and edit images

## Requirements

- **Java 17 or higher** (includes JavaFX and jpackage)
- **Maven** (for building)

## Installation

### Option 1: Run from Source

1. Clone or download this repository
2. Navigate to the project directory
3. Build and run with Maven:

```bash
mvn clean javafx:run
```

### Option 2: Create Native Installer

1. Build the application:

```bash
mvn clean package
```

2. Create native installer (requires Java 17+):

**Windows:**
```bash
bash package.sh
```
This creates a `.exe` installer in the current directory.

**macOS:**
```bash
bash package.sh
```
This creates a `.dmg` installer.

**Linux:**
```bash
bash package.sh
```
This creates both `.deb` and `.rpm` packages.

3. Install the generated package to set PhotoViewer as a default application for images.

## Usage

### Basic Operations

- **Open Image**: File → Open Image, or drag and drop
- **Zoom**: Use mouse wheel, or View menu
- **Pan**: Middle-click and drag, or Ctrl+drag
- **Save**: File → Save or Save As

### Tools

- **Select Tool**: Click to select, drag to create rectangular selection, then click "Crop" to crop
- **Scissors Tool**: Select a region, then drag it anywhere. Click "Paste" to apply
- **Pencil Tool**: Choose a color and brush size, then draw on the image. Click "Apply" to commit

### AI Integration

1. Go to **AI → Configure API Keys**
2. Enter your API key for OpenAI, Anthropic, or Google Gemini
3. Select your preferred provider in the AI panel
4. Type commands like:
   - "What's in this image?"
   - "Describe the colors and composition"
   - "What objects do you see?"

## Setting as Default Photo Viewer

### Windows
After installing the `.exe`, right-click any image file → Open with → Choose PhotoViewer → Always use this app

### macOS
Right-click an image → Get Info → Open with → PhotoViewer → Change All

### Linux
After installing the `.deb` or `.rpm`, right-click an image → Properties → Open With → PhotoViewer → Set as default

## AI API Keys

To use AI features, you need an API key from one of these providers:

- **OpenAI**: https://platform.openai.com/api-keys
- **Anthropic**: https://console.anthropic.com/
- **Google Gemini**: https://makersuite.google.com/app/apikey

## Building from Source

```bash
# Clone the repository
git clone <repository-url>
cd PhotoViewer

# Build with Maven
mvn clean package

# Run the application
mvn javafx:run
```

## Project Structure

```
PhotoViewer/
├── src/main/java/com/photoviewer/
│   ├── PhotoViewerApp.java          # Main entry point
│   ├── ui/                           # UI components
│   │   ├── MainWindow.java
│   │   ├── ImageCanvas.java
│   │   ├── ToolPanel.java
│   │   └── AIChatPanel.java
│   ├── tools/                        # Image manipulation tools
│   │   ├── SelectionTool.java
│   │   ├── ScissorsTool.java
│   │   └── PencilTool.java
│   ├── image/                        # Image operations
│   │   ├── ImageManager.java
│   │   └── ImageOperations.java
│   └── ai/                           # AI integration
│       ├── OpenAIProvider.java
│       ├── AnthropicProvider.java
│       └── GeminiProvider.java
└── pom.xml                           # Maven configuration
```

## License

MIT License - Feel free to use and modify as needed.

## Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.
