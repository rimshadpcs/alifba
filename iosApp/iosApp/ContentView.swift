//
//  ContentView.swift
//  iosApp
//
//  Created by Abdul Rashique Puthuparambil on 27/09/2024.
//

import SwiftUI
import UIKit
import shared


//struct ContentView: View {
//    var body: some View {
//        VStack {
//            Image(systemName: "globe")
//                .imageScale(.large)
//                .foregroundStyle(.tint)
//            Text("hello")
//        }
//        .padding()
//    }
//}
//
//#Preview {
//    ContentView()
//}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
                MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
                .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
    }
}
