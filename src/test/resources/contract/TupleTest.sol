pragma solidity>=0.5.0 <0.6.11;
pragma experimental ABIEncoderV2;
contract TupleTest {
    struct Item {
        int a;
        int b;
        int c;
    }

    struct Info {
        string name;
        int count;
        Item[] items;
    }

    int a;
    Item b;
    string c;

    constructor(int _a, Item memory _b, string memory _c) public {
        a = _a;
        b = _b;
        c = _c;
    }

    function set1(int _a, Item memory _b, string memory _c) public {
        a = _a;
        b = _b;
        c = _c;
    }

    function get1() view public returns(int, Item memory, string memory) {
        return (a, b, c);
    }

    function getAndSet1(int _a, Item memory _b, string memory _c) public returns(int, Item memory, string memory) {
        a = _a;
        b = _b;
        c = _c;
        return (a, b, c);
    }

    function getAndSet2(int a,Info[] memory b,string memory c) public returns (int, Info[] memory, string memory) {
        return (a, b, c);
    }

    // function set2(int _d,Info[] memory _e,string memory _f) public {
    //     d = _d;
    //     e = _e;
    //     f = _f;
    // }

    // function getAndSet2(int _d,Info[] memory _e,string memory _f) public returns (int, Info[] memory, string memory) {
    //     d = _d;
    //     e = _e;
    //     f = _f;

    //     return (d, e, f);
    // }

    function getSampleTupleValue() public view returns(int a,Info[][] memory b,string memory c){
        a=100;
        Info[][] memory infos4 = new Info[][](2);
        for(int i=0;i<10;++i){
            infos4[0] = new Info[](1);
            infos4[1] = new Info[](1);
        }

        infos4[0][0].name="Hello world! + 1 ";
        infos4[0][0].count=100;
        infos4[0][0].items= new Item[](1);
        infos4[0][0].items[0].a=1;
        infos4[0][0].items[0].b=2;
        infos4[0][0].items[0].c=3;

        infos4[1][0].name="Hello world! + 2 ";
        infos4[1][0].count=101;
        infos4[1][0].items= new Item[](1);
        infos4[1][0].items[0].a=4;
        infos4[1][0].items[0].b=5;
        infos4[1][0].items[0].c=6;

        c = "Hello world! + 3 ";
        b = infos4;
    }
}