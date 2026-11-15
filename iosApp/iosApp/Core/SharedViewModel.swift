import Shared

/// Keeps a shared Kotlin ViewModel alive while the SwiftUI view that owns it exists, then clears it
/// (cancelling its `viewModelScope`), mirroring the Android lifecycle.
///
/// Usage inside a view:
/// ```swift
/// @State private var viewModel = SharedViewModel { ViewModels.shared.home(owner: $0) }
/// ```
final class SharedViewModel<ViewModel: AnyObject> {
    let instance: ViewModel
    private let owner: IosViewModelStoreOwner

    init(_ factory: (IosViewModelStoreOwner) -> ViewModel) {
        let owner = IosViewModelStoreOwner()
        self.owner = owner
        self.instance = factory(owner)
    }

    deinit {
        owner.clear()
    }
}
