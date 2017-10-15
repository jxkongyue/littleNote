## effective modern c++
### item 1: 理解模板类型推导
auto依赖于模板类型推导
- 在模板类型推导时，有引用的实参会被视为无引用，他们的引用会被忽略
- 对于通用引用的推导，左值实参会被特殊对待
- 对于传值类型推导，实参如果具有常量性和易变性会被忽略
- 在模板类型推导时，数组或者函数实参会退化为指针，除非它们被用于初始化引用
### item 2: 理解auto类型推导
```c++
auto x1=27;         //类型是int，值是27
auto x2(27);        //同上
auto x3={27};       //类型是std::initializer_list<int>,值是{27}
auto x4{27};        //同上
```
这就造成了auto类型推导不同于模板类型推导的特殊情况。当用auto声明的变量使用花括号进行初始化，auto类型推导会推导出auto的类型为 `std::initializer_list`。如果这样的一个类型不能被成功推导（比如花括号里面包含的是不同类型的变量），编译器会拒绝这样的代码！
- auto类型推导通常和模板类型推导相同，但是auto类型推导假定花括号初始化代表std::initializer_list而模板类型推导不这样做
- 在C++14中auto允许出现在函数返回值或者lambda函数形参中，但是它的工作机制是模板类型推导那一套方案（`std::initializer_list`是模板，不支持推导）。
```c++
auto createInitList()
{
    return {1,2,3};     //错误！推导失败
}
```
### item 4: 学会查看类型推导结果
```c++
std::cout<<typeid(x).name()<<"\n";	//显示x和y的类型
std::cout<<typeid(y).name()<<"\n";
```
### item 5: 优先考虑auto而非显式类型声明
- auto变量必须初始化，通常它可以避免一些移植性和效率性的问题，也使得重构更方便，还能让你少打几个字。
- 正如Item2和6讨论的，auto类型的变量可能会踩到一些陷阱。

### item 6: 当auto推导出一个不想要的类型时，使用显式类型初始化的语法
### item 7: 当创建对象的时候，区分()和{}的使用
- 花括号初始化让你做到之前你做不到的事，使用花括号，明确容器的初始内容是很简单的：
```c++
std::vector<int> v{ 1, 3, 5};   //v的初始内容是1，3，5
```
- 花括号同样可以用来明确non-static成员变量的初始值。这是C++11的新能力，也能用”=“初始化语法做到，但是不能用圆括号做到：
```c++
class Widget{
    ...

private:
    int x{ 0 };     //对的，x的默认值为0
    int y = 0;      //同样是对的
    int z(0);       //错误！
```
- 另外，不能拷贝构造的对象（比如，std::atomics—看 Item 40）能用花括号和圆括号初始化，但是不能用”=“初始化：
```c++
std::atomic<int> ai1{ 0 };  //对的
std::atomic<int> ai2(0);    //对的
std::atomic<int> ai3 = 0;   //错误
```
- 使用花括号调用默认构造函数构造一个对象避免了被当成函数声明的问题
```c++
Widget w1(10);  //使用参数10调用Widget的构造函数
Widget w2();    //最令人恼火的解析！声明一个
                //名字是w2返回Widget的函数
Widget w3{};    //不带参数调用Widget的默认构造函数
```
- 在花括号初始化中，只有当这里没有办法转换参数的类型为std::initializer_list时，编译器才会回到正常的重载解析。
```c++
class Widget {
public:
    Widget(int i, bool b);  
    Widget(int i, double d);

    //元素类型现在是std::string
    Widget(std::initializer_list<std::string> il);  

    ...
};  

Widget w1(10, true);    //使用圆括号，和以前一样，调用
                        //第一个构造函数

Widget w2{10, true};    //使用花括号，现在调用第一个构造函数

Widget w3(10, 5.0);     //使用圆括号，和以前一样，调用
                        //第二个构造函数

Widget w4{10, 5.0};     //使用花括号，现在调用第二个构造函数
```
- 假设你使用空的花括号来构造对象，这个支持默认构造函数并且支持std::initializer_list构造函数。那么你的空的花括号意味着什么呢？
规则是你会得到默认构造函数，空的花括号意味着没有参数，不是一个空的std::initializer_list：
```c++
class Widget {
public:
    Widget();       //默认构造函数

    //std::initializer_list构造函数
    Widget(std::initializer_list<int> il);  

    ...
};  

Widget w1;      //调用默认构造函数

Widget w2{};    //也调用默认构造函数

Widget w3();    //最令人恼火的解析！声明一个函数！
```
### item 8: 比起0和NULL更偏爱nullptr
字面上的0是一个int，不是一个指针。如果C++发现0在上下文中只能被用作指针，它会勉强把0解释为一个null指针，但这只是一个应变的方案。C++的主要规则还是把0视为int，而不是一个指针。
实际上，NULL也是这样的。
```c++
oid f(int);        //f的三个重载
void f(bool);
void f(void*);

f(0);               //调用f(int), 而不是f(void*)

f(NULL);            //可能不能通过编译，但是通常会调用f(int),
                    //永远不会调用f(void*)
```
- 比起0和NULL更推荐nullptr
- 避免重载整形类型和指针类型。
### item 9: 倾向使用别名声明而非typedef
对比：
```c++
typedef std::unique_ptr<std::unordered_map<std::string, std::string>> UPtrMapSS;
using UPtrMapSS = std::unique_ptr<std::unordered_map<std::string, std::string>>;

//function point
typedef void(*FP)(int, const std::string&); // typedef
using FP = void(*)(int, const std::string&); // alias declaration

```
倾向使用别说声明的强烈原因是，别名声明可以模板化，而typedef不行.
### item 10: 优先选择scoped enums而不是unscoped enums
```c++
enum Color {black, white, red};  //black.while,red are in same scope as Color

auto white = false;              //error!white already declared in this scope
```
scoped enum:
```c++
enum class Color {black,white,red};//black,while,red are scoped to Color

auto white = false;                 //fine,no other "white" in scope

Color c = white;                    // error! no enumerator named
                                    // "white" is in this scope
Color c = Color::white;             // fine
auto c = Color::white;              // also fine (and in accord
                                    // with Item 5's advice)
```
因为scoped enum都是通过”enum class”来声明的，它们有时候也被叫做enum classes。
对于scoped enums来说，默认的潜在类型是int，但同时支持覆盖：
```c++
enum class TimeUnit:uint32_t {
	Hour,
	Minute,
	Second
};
```
### item 11: 优先选择deleted函数而不是私有未定义函数
如果你想要抑制某些成员函数的使用，这几乎总是复制构造函数或者复制操作符，亦或者两个都是。C++98中阻止这些函数被使用的方式是通过将它们定义成私有的（private）并且不定义它们。
```c++
template <class charT, class traits = char_traits<charT> >
class basic_ios : public ios_base {
public:
    …
    basic_ios(const basic_ios& ) = delete;
    basic_ios& operator=(const basic_ios&) = delete;
    …
};
```
所以使用 =delete是几乎任意函数都可以操作到，从普通函数、成员函数、模板函数、模板类成员函数等，若使用C++11，请使用=delete，而不是private禁止。
### item 12: 把重写函数声明为“override”
```c++
class Base {
public:
	virtual void mf1() const;
	virtual void mf2(int x);
	virtual void mf3() &;
	virtual void mf4() const;
};

class Derived : public Base {
public:
	virtual void mf1()  override;   //编译报错, 无法override
	virtual void mf2(int x) override;
	virtual void mf3() & override;
	void mf4() const override;              //增加“virtual”也可以，但不是必须的
};
```
### item 21: 比起直接使用new优先使用std::make_unique和std::make_shared
```c++
processWidget(std::shared_ptr<Widget>(new Widget),  //潜在的资源泄露 
              computePriority());
```
编译器可能产生出这样顺序的代码：
- 执行“new Widget”。
- 执行computePriority。
- 执行std::shared_ptr的构造函数。
如果第二步抛出异常，那么第一步的内存就被泄露
使用std::make_shared可以避免这样的问题。调用代码将看起来像这样：
```c++
processWidget(std::make_shared<Widget>(),       //没有资源泄露
              computePriority());    
```
std::make_shared（比起直接使用new）的一个特性是能提升效率。使用std::make_shared允许编译器产生更小，更快的代码，产生的代码使用更简洁的数据结构。考虑下面直接使用new的代码：
```c++
std::shared_ptr<Widget> spw(new Widget);
```
如果使用std::make_shared来替换，
```c++
auto spw = std::make_shared<Widget>();
```
一次分配就足够了。这是因为std::make_shared申请一个单独的内存块来同时存放Widget对象和控制块。这个优化减少了程序的静态大小，因为代码只包含一次内存分配的调用，并且这会加快代码的执行速度，因为内存只分配了一次。另外，使用std::make_shared消除了一些控制块需要记录的信息，这样潜在地减少了程序的总内存占用。

make函数完美转发它的参数给一个对象的构造函数，但是它们应该使用圆括号还是花括号呢？对于一些类型，不同的答案会影响很大。举个例子，在这些调用中，
```c++
auto upv = std::make_unique<std::vector<int>>(10, 20);
auto spv = std::make_shared<std::vector<int>>(10, 20);
```
结果是一个智能指针指向一个std::vector,这个std::vector应该带有10个元素，每个元素的值是20，还是说这个std::vector应该带2个元素，一个值是10，一个值是20？还是说结果应该是不确定的？

一个好消息是，它不是不确定的：两个调用创建的std::vector都带10个元素，每个元素的值被设置为20.这意味着使用make函数，完美转发代码使用圆括号，而不是花括号。坏消息是如果你想使用花括号来构造你要指向的对象，你必须直接使用new。

- 对比直接使用new，make函数消除了源代码的重复，提升了异常安全性，并且对于std::make_shared和std::allocate_shared，产生的代码更小更快。
- 不适合使用make函数的情况包括：需要指定自定义deleter，需要传入初始化列表。
- 对于std::shared_ptr，额外使用make函数的欠考虑的情况包括（1）有自定义内存管理的类和（2）需要关心内存，对象很大，std::weak_ptr比对应的std::shared_ptr存在得久的系统。
