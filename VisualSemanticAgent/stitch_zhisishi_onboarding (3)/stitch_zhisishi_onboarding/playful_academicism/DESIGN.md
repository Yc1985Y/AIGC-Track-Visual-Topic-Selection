---
name: Playful Academicism
colors:
  surface: '#fff8f2'
  surface-dim: '#ffd47e'
  surface-bright: '#fff8f2'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#fff2df'
  surface-container: '#ffebcb'
  surface-container-high: '#ffe5b6'
  surface-container-highest: '#ffdea1'
  on-surface: '#261900'
  on-surface-variant: '#404945'
  inverse-surface: '#402d00'
  inverse-on-surface: '#ffefd5'
  outline: '#707975'
  outline-variant: '#bfc9c3'
  surface-tint: '#2e6857'
  primary: '#003528'
  on-primary: '#ffffff'
  primary-container: '#0b4d3d'
  on-primary-container: '#82bda8'
  inverse-primary: '#97d3bd'
  secondary: '#9e3f42'
  on-secondary: '#ffffff'
  secondary-container: '#fe8989'
  on-secondary-container: '#762125'
  tertiary: '#4e2019'
  on-tertiary: '#ffffff'
  tertiary-container: '#69352d'
  on-tertiary-container: '#e79f93'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#b2efd9'
  primary-fixed-dim: '#97d3bd'
  on-primary-fixed: '#002118'
  on-primary-fixed-variant: '#105040'
  secondary-fixed: '#ffdad8'
  secondary-fixed-dim: '#ffb3b1'
  on-secondary-fixed: '#410007'
  on-secondary-fixed-variant: '#7f282c'
  tertiary-fixed: '#ffdad4'
  tertiary-fixed-dim: '#ffb4a8'
  on-tertiary-fixed: '#370e08'
  on-tertiary-fixed-variant: '#6c3830'
  background: '#fff8f2'
  on-background: '#261900'
  surface-variant: '#ffdea1'
typography:
  display-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '800'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '500'
    lineHeight: 28px
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-caps:
    fontFamily: Lexend
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
rounded:
  sm: 0.5rem
  DEFAULT: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  full: 9999px
spacing:
  margin-page: 24px
  gutter-grid: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 32px
---

## Brand & Style

This design system is built for the modern student—balancing the rigor of campus life with a soft, approachable aesthetic that reduces academic anxiety. The visual direction moves away from rigid, data-heavy dashboards in favor of a "Playful Academicism" style. It leverages organic shapes and a cheerful palette to make productivity feel like a rewarding habit rather than a chore.

The system combines **Minimalism** with subtle **Glassmorphism**. High-density information is broken down into digestible, colorful "nuggets" of content. The emotional response is intended to be optimistic, lightweight, and encouraging, mirroring the supportive environment of a digital study companion.

## Colors

The palette is anchored by a warm, paper-like cream background to reduce eye strain during long study sessions. **Deep Teal** (#0b4d3d) is reserved strictly for primary actions and critical navigational elements to provide a grounded sense of authority.

The secondary and accent colors—**Coral** (#ff8a8a) and **Goldenrod** (#eaaf0e)—act as functional category markers. They should be used for card backgrounds, progress indicators, and tag categorization. These colors are intended to be mixed, creating a vibrant, scrapbook-like feel across the interface without compromising readability.

## Typography

This design system utilizes **Plus Jakarta Sans** for its friendly, open counters and modern geometric construction. Headlines are intentionally heavy and bold to create a clear visual hierarchy against the soft background.

For specialized data points and labels, **Lexend** is employed due to its specific design for readability and focus—perfect for time-trackers and grade displays. On mobile devices, line heights are generous to ensure the interface feels "airy" and accessible.

## Layout & Spacing

The layout follows a **fluid grid** model optimized for handheld use. A standard 4-column mobile grid is used with generous 24px side margins to prevent the UI from feeling cramped. 

Spacing is driven by an 8px base unit. Component containers use internal padding of at least 20px to accommodate the large corner radii. Content should be grouped into distinct "cards" rather than separated by lines, using vertical stack spacing to define relationships between subjects or tasks.

## Elevation & Depth

Depth is achieved through **Ambient Shadows** and **Tonal Layers** rather than heavy outlines. 
1.  **Level 0 (Surface):** The cream-white background.
2.  **Level 1 (Cards):** Soft white or pastel-tinted cards with a very diffuse, low-opacity shadow (Color: #0B4D3D at 5% opacity, Y: 8, Blur: 24).
3.  **Level 2 (Overlays/Modals):** Semi-transparent frosted glass layers with a 20px backdrop blur, used for navigation bars and pop-up alerts.

Avoid pure black shadows; always tint shadows with the primary deep teal or the specific accent color of the card to maintain the soft, youthful glow.

## Shapes

The shape language is defined by **ultra-rounded corners**. A base radius of 24px is applied to all standard cards and containers. Smaller elements like buttons and chips should utilize a fully rounded (pill) shape to emphasize the friendly, non-corporate nature of the app.

Interactive elements should feel "squishy" and tactile. Avoid any sharp 90-degree angles in the UI to maintain the soft aesthetic consistent with educational and lifestyle tracking apps.

## Components

### Buttons & Actions
Primary buttons use the Deep Teal background with white text, utilizing a pill-shape and a subtle "lift" shadow. Secondary buttons should use semi-transparent versions of the accent colors (e.g., 20% opacity Coral or Goldenrod) to keep the weight light.

### Cards & Chips
Cards are the primary content vessel. Each card should have a 24px+ corner radius and a subtle tinted background. Chips are used for tagging "Subjects" or "Priority" and should always be pill-shaped with high-contrast text against a saturated pastel background.

### Input Fields
Inputs should be borderless with a soft off-white or light beige inner fill. The focus state is indicated by a 2px Deep Teal or Goldenrod glow. Labels should sit above the field in the **Lexend** label-caps style.

### Icons
Icons must be linear with a 2px stroke width and rounded terminals. Avoid filled icons unless used as a "selected" state in the bottom navigation bar. Use a slightly larger optical size to match the bold nature of the typography.

### Progress Indicators
Use thick, rounded stroke lines for progress bars and circular trackers. Incorporate gradients between accent colors (e.g., Coral to Goldenrod) to represent growth and completion.