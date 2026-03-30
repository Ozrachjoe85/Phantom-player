# Contributing to Phantom Player

First off, thank you for considering contributing to Phantom Player! 🎵

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)

---

## Code of Conduct

By participating in this project, you agree to maintain a respectful and collaborative environment. Be kind, professional, and constructive in all interactions.

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Git
- Basic knowledge of Kotlin and Jetpack Compose

### First Time Setup

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/phantom-player.git
   cd phantom-player
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/Ozrachjoe85/phantom-player.git
   ```
4. **Open in Android Studio** and let Gradle sync complete

---

## Development Setup

### Building the Project

```bash
# Debug build
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on device
./gradlew installDebug
```

### Project Structure

```
app/src/main/java/com/phantom/player/
├── data/          # Data layer (repositories, database, scanning)
├── domain/        # Business logic (audio processing)
├── di/            # Dependency injection modules
├── service/       # Background services
└── ui/            # UI layer (screens, viewmodels, theme)
```

---

## How to Contribute

### Reporting Bugs

1. **Check existing issues** to avoid duplicates
2. **Create a new issue** with:
   - Clear, descriptive title
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
   - Device info (model, Android version)

### Suggesting Features

1. **Open an issue** with the `enhancement` label
2. **Describe the feature** clearly:
   - What problem does it solve?
   - How should it work?
   - Any UI mockups or examples?

### Contributing Code

1. **Pick an issue** or create one for your planned work
2. **Comment on the issue** to claim it
3. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **Make your changes** following the coding standards
5. **Test thoroughly** on multiple devices/API levels
6. **Commit your changes** with clear messages
7. **Push to your fork**:
   ```bash
   git push origin feature/your-feature-name
   ```
8. **Open a Pull Request** to the `main` branch

---

## Coding Standards

### Kotlin Style

Follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html):

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable names
- Prefer `val` over `var`
- Use trailing commas in multi-line declarations

### Naming Conventions

```kotlin
// Classes: PascalCase
class PlayerViewModel

// Functions: camelCase
fun togglePlayPause()

// Constants: SCREAMING_SNAKE_CASE
const val MAX_BUFFER_SIZE = 1024

// Private properties: start with underscore
private val _isPlaying = MutableStateFlow(false)
```

### Composable Functions

```kotlin
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // Component implementation
}
```

### Comments

- Use `//` for single-line comments
- Use `/** */` for function documentation
- Explain *why*, not *what* (code should be self-documenting)

```kotlin
/**
 * Applies the current EQ settings to the audio session.
 * 
 * @param audioSessionId The audio session to apply EQ to
 * @return true if successful, false otherwise
 */
fun applyEqualizer(audioSessionId: Int): Boolean
```

---

## Commit Guidelines

### Commit Message Format

```
type(scope): subject

body (optional)

footer (optional)
```

### Types

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, no logic change)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

### Examples

```bash
feat(player): add shuffle and repeat modes

Implemented shuffle and repeat functionality with persistent state.
Added UI controls in the player screen.

Closes #42

---

fix(eq): correct frequency range calculation

Fixed off-by-one error in EQ band frequency mapping.

---

docs(readme): update installation instructions
```

---

## Pull Request Process

### Before Submitting

- [ ] Code follows the style guidelines
- [ ] Code compiles without warnings
- [ ] App runs on Android 8.0+ devices
- [ ] No new lint errors introduced
- [ ] All existing functionality still works
- [ ] Commit messages follow the guidelines
- [ ] Branch is up to date with `main`

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- Device tested: [e.g., Pixel 6, Android 13]
- Test scenarios: [describe what you tested]

## Screenshots (if applicable)
[Add screenshots or screen recordings]

## Related Issues
Closes #123
```

### Review Process

1. **Automated checks** must pass (build, lint)
2. **Code review** by maintainer
3. **Testing** on multiple devices
4. **Merge** once approved

### After Merge

- Your contribution will appear in the next release
- You'll be added to the contributors list
- Thank you! 🎉

---

## Development Tips

### Testing on Device

```bash
# Install debug build
./gradlew installDebug

# View logs
adb logcat | grep PhantomPlayer

# Clear app data
adb shell pm clear com.phantom.player
```

### Debugging Compose UI

```kotlin
// Enable Compose layout inspector
modifier = Modifier
    .semantics { contentDescription = "PlayerScreen" }
```

### Performance Profiling

- Use Android Studio Profiler for CPU/Memory analysis
- Enable GPU rendering profiler in Developer Options
- Monitor frame rates during visualizer animations

---

## Questions?

- **General questions**: Open a GitHub Discussion
- **Bug reports**: Open an Issue
- **Feature requests**: Open an Issue with `enhancement` label

---

## Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes
- In-app credits (future feature)

Thank you for contributing to Phantom Player! 💜
