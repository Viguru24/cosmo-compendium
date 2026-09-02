import SwiftUI

public struct RealisticLeatherCover<Content: View>: View {
    public let theme: CoverTheme
    public let isCard: Bool
    public let content: () -> Content

    public init(
        theme: CoverTheme = .vintageLeather,
        isCard: Bool = false,
        @ViewBuilder content: @escaping () -> Content = { EmptyView() }
    ) {
        self.theme = theme
        self.isCard = isCard
        self.content = content
    }

    public var body: some View {
        ZStack {
            Canvas { ctx, size in
                let w = size.width
                let h = size.height
                guard w > 0, h > 0 else { return }

                // 1. Base Rich Leather Radial Gradient with Warm Highlight
                let baseRect = CGRect(origin: .zero, size: size)
                let gradient = Gradient(colors: [
                    theme.primaryColor,
                    theme.secondaryColor,
                    Color(red: 0x1E / 255.0, green: 0x0A / 255.0, blue: 0x04 / 255.0)
                ])
                ctx.fill(
                    Path(baseRect),
                    with: .radialGradient(
                        gradient,
                        center: CGPoint(x: w * 0.45, y: h * 0.38),
                        startRadius: 0.0,
                        endRadius: max(w, h) * 0.95
                    )
                )

                // 2. Leather Patina Mottling
                drawLeatherMottling(ctx: &ctx, w: w, h: h)

                // 3. Scorched Corners (Charred carbon burn falloff)
                drawScorchedCorners(ctx: &ctx, w: w, h: h)

                // 4. Kitchen Patina (Coffee / Oil Stains)
                drawKitchenStains(ctx: &ctx, w: w, h: h)

                // 5. Book Spine Hinge Groove & Saddle Stitching
                drawSpineAndStitching(ctx: &ctx, w: w, h: h)

                // 6. Antique Weathered Brass Corner Brackets
                drawAntiqueBrassCorners(ctx: &ctx, w: w, h: h)
            }

            content()
        }
        .clipShape(RoundedRectangle(cornerRadius: isCard ? 10 : 14, style: .continuous))
        .shadow(color: Color.black.opacity(0.4), radius: isCard ? 6 : 12, x: 2, y: 5)
    }

    private func drawLeatherMottling(ctx: inout GraphicsContext, w: CGFloat, h: CGFloat) {
        let spots: [(CGPoint, CGFloat, Double)] = [
            (CGPoint(x: w * 0.25, y: h * 0.2), w * 0.35, 0.08),
            (CGPoint(x: w * 0.75, y: h * 0.4), w * 0.40, 0.07),
            (CGPoint(x: w * 0.30, y: h * 0.7), w * 0.38, 0.09),
            (CGPoint(x: w * 0.65, y: h * 0.8), w * 0.42, 0.08)
        ]

        for (center, radius, alpha) in spots {
            var path = Path()
            path.addEllipse(in: CGRect(x: center.x - radius, y: center.y - radius, width: radius * 2, height: radius * 2))
            ctx.fill(path, with: .color(Color.black.opacity(alpha)))
        }
    }

    private func drawScorchedCorners(ctx: inout GraphicsContext, w: CGFloat, h: CGFloat) {
        let burnRadius = min(w, h) * (isCard ? 0.35 : 0.45)
        let corners = [
            CGPoint(x: 0, y: 0),
            CGPoint(x: w, y: 0),
            CGPoint(x: 0, y: h),
            CGPoint(x: w, y: h)
        ]

        for corner in corners {
            let burnGrad = Gradient(colors: [
                Color.black.opacity(0.75),
                Color(red: 0x22 / 255.0, green: 0x0C / 255.0, blue: 0x05 / 255.0).opacity(0.4),
                Color.clear
            ])
            var path = Path()
            path.addEllipse(in: CGRect(x: corner.x - burnRadius, y: corner.y - burnRadius, width: burnRadius * 2, height: burnRadius * 2))
            ctx.fill(
                path,
                with: .radialGradient(burnGrad, center: corner, startRadius: 0.0, endRadius: burnRadius)
            )
        }
    }

    private func drawKitchenStains(ctx: inout GraphicsContext, w: CGFloat, h: CGFloat) {
        // Subtle coffee ring stain
        let ringCenter = CGPoint(x: w * 0.72, y: h * 0.35)
        let ringRadius = min(w, h) * 0.16
        var ringPath = Path()
        ringPath.addEllipse(in: CGRect(x: ringCenter.x - ringRadius, y: ringCenter.y - ringRadius, width: ringRadius * 2, height: ringRadius * 2))
        ctx.stroke(ringPath, with: .color(Color(red: 0x3E / 255.0, green: 0x27 / 255.0, blue: 0x23 / 255.0).opacity(0.12)), lineWidth: 3.5)
    }

    private func drawSpineAndStitching(ctx: inout GraphicsContext, w: CGFloat, h: CGFloat) {
        let spineX = isCard ? w * 0.08 : w * 0.07

        // Spine shadow groove
        var spinePath = Path()
        spinePath.move(to: CGPoint(x: spineX, y: 0))
        spinePath.addLine(to: CGPoint(x: spineX, y: h))
        ctx.stroke(spinePath, with: .color(Color.black.opacity(0.55)), lineWidth: 3.0)

        // Spine highlight
        var highlightPath = Path()
        highlightPath.move(to: CGPoint(x: spineX + 2, y: 0))
        highlightPath.addLine(to: CGPoint(x: spineX + 2, y: h))
        ctx.stroke(highlightPath, with: .color(Color.white.opacity(0.12)), lineWidth: 1.5)

        // Waxed Saddle Stitching (Dashed line inset from edge)
        let inset = isCard ? 7.0 : 12.0
        let stitchRect = CGRect(x: inset, y: inset, width: w - (inset * 2), height: h - (inset * 2))
        let stitchPath = Path(roundedRect: stitchRect, cornerRadius: 6)

        ctx.stroke(
            stitchPath,
            with: .color(theme.goldFoilColor.opacity(0.4)),
            style: StrokeStyle(lineWidth: 1.2, lineCap: .round, dash: [4, 4])
        )
    }

    private func drawAntiqueBrassCorners(ctx: inout GraphicsContext, w: CGFloat, h: CGFloat) {
        let bracketSize: CGFloat = isCard ? 14.0 : 24.0
        let brassColor = theme.brassCornerColor

        // Top-left
        var tl = Path()
        tl.move(to: CGPoint(x: 0, y: bracketSize))
        tl.addLine(to: CGPoint(x: 0, y: 0))
        tl.addLine(to: CGPoint(x: bracketSize, y: 0))
        tl.addLine(to: CGPoint(x: bracketSize * 0.5, y: bracketSize * 0.5))
        tl.closeSubpath()
        ctx.fill(tl, with: .color(brassColor.opacity(0.85)))

        // Top-right
        var tr = Path()
        tr.move(to: CGPoint(x: w - bracketSize, y: 0))
        tr.addLine(to: CGPoint(x: w, y: 0))
        tr.addLine(to: CGPoint(x: w, y: bracketSize))
        tr.addLine(to: CGPoint(x: w - (bracketSize * 0.5), y: bracketSize * 0.5))
        tr.closeSubpath()
        ctx.fill(tr, with: .color(brassColor.opacity(0.85)))

        // Bottom-left
        var bl = Path()
        bl.move(to: CGPoint(x: 0, y: h - bracketSize))
        bl.addLine(to: CGPoint(x: 0, y: h))
        bl.addLine(to: CGPoint(x: bracketSize, y: h))
        bl.addLine(to: CGPoint(x: bracketSize * 0.5, y: h - (bracketSize * 0.5)))
        bl.closeSubpath()
        ctx.fill(bl, with: .color(brassColor.opacity(0.85)))

        // Bottom-right
        var br = Path()
        br.move(to: CGPoint(x: w - bracketSize, y: h))
        br.addLine(to: CGPoint(x: w, y: h))
        br.addLine(to: CGPoint(x: w, y: h - bracketSize))
        br.addLine(to: CGPoint(x: w - (bracketSize * 0.5), y: h - (bracketSize * 0.5)))
        br.closeSubpath()
        ctx.fill(br, with: .color(brassColor.opacity(0.85)))
    }
}
