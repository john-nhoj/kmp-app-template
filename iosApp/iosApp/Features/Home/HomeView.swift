import Shared
import SwiftUI

/// Stateful entry point: owns the shared `HomeViewModel` and observes its `uiState`.
struct HomeView: View {
    @State private var viewModel = SharedViewModel { ViewModels.shared.home(owner: $0) }
    @State private var uiState = HomeUiState(isLoading: true, platformName: nil, isError: false)

    var body: some View {
        HomeContent(uiState: uiState, onRefresh: { viewModel.instance.onRefresh() })
            .task {
                // SKIE exposes Kotlin's StateFlow as an AsyncSequence. The task is cancelled with the view.
                for await state in viewModel.instance.uiState {
                    uiState = state
                }
            }
    }
}

/// Stateless and previewable: renders `uiState` and reports user intents through closures.
struct HomeContent: View {
    let uiState: HomeUiState
    let onRefresh: () -> Void

    var body: some View {
        NavigationStack {
            content
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding()
                .navigationTitle("Home")
                .toolbar {
                    ToolbarItem(placement: .primaryAction) {
                        Button("Refresh", systemImage: "arrow.clockwise", action: onRefresh)
                            .disabled(uiState.isLoading)
                    }
                }
        }
    }

    @ViewBuilder
    private var content: some View {
        if uiState.isLoading {
            ProgressView()
        } else if uiState.isError {
            ContentUnavailableView {
                Label("Something went wrong.", systemImage: "exclamationmark.triangle")
            } actions: {
                Button("Try Again", action: onRefresh)
                    .buttonStyle(.borderedProminent)
            }
        } else if let platformName = uiState.platformName {
            greeting(Text("Hello, \(platformName)!"))
        } else {
            greeting(Text("Hello!"))
        }
    }

    private func greeting(_ text: Text) -> some View {
        text
            .font(.largeTitle.bold())
            .multilineTextAlignment(.center)
    }
}

#Preview("Content") {
    HomeContent(uiState: HomeUiState(isLoading: false, platformName: "iOS 26", isError: false), onRefresh: {})
}

#Preview("Error") {
    HomeContent(uiState: HomeUiState(isLoading: false, platformName: nil, isError: true), onRefresh: {})
}
