### C++单例设计总结
#### 1.  使用局部静态变量方式
```c++
class Singleton {
public:
    static Singleton &getInstance();

private:
    Singleton() {}
    Singleton(const Singleton &) = delete;
    Singleton &operator=(const Singleton &) = delete;
};


Singleton &Singleton::getInstance() {
    static Singleton instance;
    return instance;
}

```
在C++ 11版本中，局部静态变量初始化是线程安全的。在C++ 98 版本无法保证线程安全。

#### 2. 使用`pthread_once`
```c++
class Singleton {
public:
    static Singleton *getInstance();
    
private:
    Singleton() {}
    Singleton(const Singleton &) = delete;
    Singleton &operator=(const Singleton &) = delete;

    static void init() {
        instance_ = new Singleton();
    }

    static Singleton *instance_;
    static pthread_once_t ponce_;
};

Singleton *Singleton::getInstance() {
    pthread_once(&ponce_, &Singleton::init);
    return instance_;
}

pthread_once_t Singleton::ponce_ = PTHREAD_ONCE_INIT;
Singleton *Singleton::instance_ = nullptr;
```
在多线程环境下，尽管`pthread_once()`调用会出现在多个线程中，`init_routine()`函数仅执行一次，究竟在哪个线程中执行是不定的，是由内核调度来决定。
该方式可以保证单例的线程安全。
#### 3. Double Check Lock (DCL)的方式
如果要保证单例对象初始化一次，可以在`getInstance() `函数内加锁，确保初始化代码块只能被一个线程执行。最简单的形式如下：
```c++
class Singleton {
public:
    static Singleton *getInstance();
private:
    Singleton() {}
    Singleton(const Singleton &) = delete;
    Singleton &operator=(const Singleton &) = delete;

    static Singleton *instance_;
    static mutex mutex_;
};

Singleton *Singleton::instance_ = nullptr;
mutex Singleton::mutex_;

Singleton *Singleton::getInstance() {
    lock_guard<mutex> lock(mutex_);
    if (instance_ == nullptr) {
        instance_ = new Singleton();
    }
    return instance_;
}

```
这种方式，确保了`instance_ `只被第一次获取锁的线程初始化，线程安全。但是每次调用`getInstance()`获取单例，都需要获取锁，锁竞争影响了性能。
Double Check Lock的方式就是为了解决上述问题，实现如下：
```c++
/**
 * 
 * @NotThreadSafe
 */
Singleton *Singleton::getInstance() {
    Singleton *tmp = instance_;
    if (tmp == nullptr) {
        lock_guard<mutex> lock(mutex_);
        tmp = instance_;
        if (tmp == nullptr) {
            tmp = new Singleton();
            instance_ = tmp;
        }
    }
    return tmp;
}
```
当某个线程完成实例初始化后，再次调用获取实例的函数，可以直接返回实例，不会走到加锁的判断逻辑，避免了加锁对性能的影响。
但是，上述代码没有考虑多核场景下的“缓存一致性”的问题。现代CPU一般有多级缓存，执行时大多直接与缓存区打交道。L1级缓存一般是CPU独占，这就可能导致某个线程获取实例后，另外的线程仍然读取不到该值。
解决上述问题，一种方式是在代码中加入内存屏障，确保内存一致：
```
atomic<Singleton *> Singleton::instance_;
mutex Singleton::mutex_;

Singleton *Singleton::getInstance() {
    Singleton *tmp = instance_.load(memory_order_relaxed);
    atomic_thread_fence(memory_order_acquire); 
    if (tmp == nullptr) {
        lock_guard<mutex> lock(mutex_);
        tmp = instance_.load(memory_order_relaxed);
        if (tmp == nullptr) {
            tmp = new Singleton();
            atomic_thread_fence(memory_order_release);
            instance_.store(tmp, memory_order_relaxed);
        }
    }
    return tmp;
}

```
为了确保可靠，将`instance_`定义成原子变量。

关于DCL，还有其他实现方式，见参考中的链接。

####参考：

[Double-Checked Locking is Fixed In C++11](http://preshing.com/20130930/double-checked-locking-is-fixed-in-cpp11/)

> Written with [StackEdit](https://stackedit.io/).
