import ProjectDescription

let project = Project(
    name: "BudgetManager",
    targets: [
        .target(
            name: "BudgetManager",
            destinations: .iOS,
            product: .app,
            bundleId: "com.budgetmanager.app",
            infoPlist: .extendingDefault(
                with: [
                    "UILaunchScreen": [
                        "UIColorName": "",
                        "UIImageName": "",
                    ],
                ]
            ),
            sources: ["Sources/**"],
            resources: ["Sources/Resources/**"],
            dependencies: []
        ),
        .target(
            name: "BudgetManagerTests",
            destinations: .iOS,
            product: .unitTests,
            bundleId: "com.budgetmanager.tests",
            infoPlist: .default,
            sources: ["Tests/**"],
            dependencies: [
                .target(name: "BudgetManager")
            ]
        ),
    ]
)
