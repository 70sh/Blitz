# Blitz

> Contributions are more than welcome! 

### Importing Blitz into your project:
```gradle
repositories {
    maven { url = 'https://jitpack.io' }
}
dependencies { 
    implementation 'com.github.CafeDevelopment:Blitz:VERSION' {
        // If you aren't using Reflections in your project, comment this out:
        exclude group: 'org.relections'
    }
}
```

### Usage:
[Example](https://github.com/cafedevelopment/blitz/blob/master/src/main/java/club/cafedevelopment/blitz/example/ExampleUsage.java)

### Contributing:
    - Make your own fork.
    - git clone https://github.com/YOUR_NAME/Blitz.git.
    - Push updates to your fork.
    - Make a Pull Request when you want to merge your changes.
    
### Currently, known bugs are:
    - Multithreading will cause objects to not register/unregister properly.
    
### Roadmap:
    - Fix multithreading registering/unregistering
    - Make EventDispatcher.SYSTEM_EVENT_DISPATCHER a system-wide Event Dispatcher.
    
#### Special Thanks to:
[cats](https://github.com/vialdevelopment) for code review and (a lot of) improvement suggestions

[ollie](https://github.com/olliem5) for using this in his products and in that, testing the project

[bon](https://github.com/bon55) for being based and learning new things with me :D