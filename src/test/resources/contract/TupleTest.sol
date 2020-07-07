pragma solidity>=0.5.0 <0.6.0;
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

    function getAndSet(int a,Info[] memory b,string memory c) public returns (int,Info[] memory, string memory){
        return (a,b,c);
    }

    function getValue() public view returns(int a,Info[][] memory b,string memory c){
        a=100;
        Info[][] memory infos4 = new Info[][](1);
        for(int i=0;i<10;++i){
            infos4[0] = new Info[](1);
        }

        infos4[0][0].name="Hello world!";
        infos4[0][0].count=100;
        infos4[0][0].items= new Item[](1);
        infos4[0][0].items[0].a=1;
        infos4[0][0].items[0].b=2;
        infos4[0][0].items[0].c=3;
        c = "Hello world!";
        b = infos4;
    }
}