import UIKit
import PDFKit

public enum RecipePdfGenerator {

    public static func generatePdf(for recipe: Recipe, unitSystem: UnitSystem = .ukImperial) -> Data {
        let pageWidth: CGFloat = 595.2 // A4 width at 72 dpi
        let pageHeight: CGFloat = 841.8 // A4 height at 72 dpi
        let pageRect = CGRect(x: 0, y: 0, width: pageWidth, height: pageHeight)

        let renderer = UIGraphicsPDFRenderer(bounds: pageRect)

        return renderer.pdfData { context in
            context.beginPage()

            let margin: CGFloat = 40.0
            let contentWidth = pageWidth - (margin * 2)

            // Draw decorative vintage border
            let borderRect = CGRect(x: margin / 2, y: margin / 2, width: pageWidth - margin, height: pageHeight - margin)
            let borderPath = UIBezierPath(roundedRect: borderRect, cornerRadius: 8.0)
            UIColor(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0, alpha: 0.8).setStroke()
            borderPath.lineWidth = 2.0
            borderPath.stroke()

            let innerRect = borderRect.insetBy(dx: 4, dy: 4)
            let innerPath = UIBezierPath(roundedRect: innerRect, cornerRadius: 6.0)
            UIColor(red: 0xC8 / 255.0, green: 0x9B / 255.0, blue: 0x3C / 255.0, alpha: 0.6).setStroke()
            innerPath.lineWidth = 0.75
            innerPath.stroke()

            var cursorY: CGFloat = margin + 15.0

            // Title
            let titleFont = UIFont(name: "Georgia-Bold", size: 22) ?? UIFont.boldSystemFont(ofSize: 22)
            let titleAttributes: [NSAttributedString.Key: Any] = [
                .font: titleFont,
                .foregroundColor: UIColor(red: 0x3E / 255.0, green: 0x27 / 255.0, blue: 0x23 / 255.0, alpha: 1.0)
            ]
            let titleString = recipe.displayTitle()
            let titleSize = titleString.size(withAttributes: titleAttributes)
            titleString.draw(at: CGPoint(x: margin, y: cursorY), withAttributes: titleAttributes)
            cursorY += titleSize.height + 6.0

            // Metadata row (Category, Prep, Cook, Servings)
            let metaFont = UIFont(name: "Georgia-Italic", size: 12) ?? UIFont.italicSystemFont(ofSize: 12)
            let metaText = "\(recipe.category)  •  Prep: \(recipe.prepTimeMinutes)m  •  Cook: \(recipe.cookTimeMinutes)m  •  \(recipe.servings)  •  System: \(unitSystem.shortLabel)"
            let metaAttributes: [NSAttributedString.Key: Any] = [
                .font: metaFont,
                .foregroundColor: UIColor(red: 0x5D / 255.0, green: 0x40 / 255.0, blue: 0x37 / 255.0, alpha: 1.0)
            ]
            metaText.draw(at: CGPoint(x: margin, y: cursorY), withAttributes: metaAttributes)
            cursorY += 24.0

            // Divider line
            let dividerPath = UIBezierPath()
            dividerPath.move(to: CGPoint(x: margin, y: cursorY))
            dividerPath.addLine(to: CGPoint(x: margin + contentWidth, y: cursorY))
            UIColor(red: 0xD7 / 255.0, green: 0xCC / 255.0, blue: 0xC8 / 255.0, alpha: 1.0).setStroke()
            dividerPath.lineWidth = 1.0
            dividerPath.stroke()
            cursorY += 16.0

            // Ingredients Section Header
            let sectionHeaderFont = UIFont(name: "Georgia-Bold", size: 15) ?? UIFont.boldSystemFont(ofSize: 15)
            let sectionAttributes: [NSAttributedString.Key: Any] = [
                .font: sectionHeaderFont,
                .foregroundColor: UIColor(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0, alpha: 1.0)
            ]
            "INGREDIENTS".draw(at: CGPoint(x: margin, y: cursorY), withAttributes: sectionAttributes)
            cursorY += 20.0

            let bodyFont = UIFont(name: "Georgia", size: 11) ?? UIFont.systemFont(ofSize: 11)
            let bodyAttributes: [NSAttributedString.Key: Any] = [
                .font: bodyFont,
                .foregroundColor: UIColor(red: 0x2E / 255.0, green: 0x1B / 255.0, blue: 0x13 / 255.0, alpha: 1.0)
            ]

            var currentGroup: String? = nil
            for ing in recipe.ingredients {
                if let grp = ing.group, grp != currentGroup {
                    currentGroup = grp
                    cursorY += 4.0
                    let groupFont = UIFont(name: "Georgia-BoldItalic", size: 11) ?? UIFont.boldSystemFont(ofSize: 11)
                    grp.draw(at: CGPoint(x: margin + 8, y: cursorY), withAttributes: [.font: groupFont, .foregroundColor: UIColor(red: 0x8D / 255.0, green: 0x6E / 255.0, blue: 0x63 / 255.0, alpha: 1.0)])
                    cursorY += 15.0
                }

                let amt = ing.convertedAmount(targetSystem: unitSystem)
                let text = "•  \(amt.isEmpty ? "" : "\(amt) ")\(ing.displayName())"
                text.draw(at: CGPoint(x: margin + 12, y: cursorY), withAttributes: bodyAttributes)
                cursorY += 15.0
            }

            cursorY += 14.0

            // Instructions Section Header
            "DIRECTIONS".draw(at: CGPoint(x: margin, y: cursorY), withAttributes: sectionAttributes)
            cursorY += 20.0

            for step in recipe.steps {
                let stepHeader = "Step \(step.stepNumber):"
                let stepHeaderAttributes: [NSAttributedString.Key: Any] = [
                    .font: UIFont(name: "Georgia-Bold", size: 11) ?? UIFont.boldSystemFont(ofSize: 11),
                    .foregroundColor: UIColor(red: 0x78 / 255.0, green: 0x35 / 255.0, blue: 0x0F / 255.0, alpha: 1.0)
                ]
                stepHeader.draw(at: CGPoint(x: margin + 8, y: cursorY), withAttributes: stepHeaderAttributes)
                cursorY += 15.0

                let instruction = step.instruction(unitSystem: unitSystem)
                let rect = CGRect(x: margin + 8, y: cursorY, width: contentWidth - 16, height: 60)
                instruction.draw(in: rect, withAttributes: bodyAttributes)
                cursorY += 40.0
            }

            // Footer notes
            if !recipe.notes.isEmpty {
                cursorY = max(cursorY + 10.0, pageHeight - margin - 50.0)
                let noteFont = UIFont(name: "Georgia-Italic", size: 10) ?? UIFont.italicSystemFont(ofSize: 10)
                let noteText = "Family Note: \(recipe.notes)"
                noteText.draw(in: CGRect(x: margin, y: cursorY, width: contentWidth, height: 40), withAttributes: [.font: noteFont, .foregroundColor: UIColor.darkGray])
            }
        }
    }
}
