import Shared
import SwiftUI

@main
struct TemplateApp: App {
    init() {
        AppInitializer.shared.start()
    }

    var body: some Scene {
        WindowGroup {
            HomeView()
        }
    }
}
