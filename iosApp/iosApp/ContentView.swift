import UIKit
import SwiftUI
import ComposeApp
import EventKit

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        
      return  MainViewControllerKt.ComposeEntryPointWithUIViewController(createUIViewController: { () -> UIViewController in
            let swiftUIView = VStack {
                
            }
            return UIHostingController(rootView: swiftUIView)
        })
        
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        
        
    }
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .padding(.bottom, -40)
           // .padding(.top, -60)
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .onAppear(){
                MainViewControllerKt.changeScreenOrientation { KotlinInt in
                    print(KotlinInt)
                }
            }
    }
}



