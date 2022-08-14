/**********************************************/
/* C++ generated from LogScope specification! */
/**********************************************/

#include "contract.h"

using namespace std;

ast::Monitor *monitorM4() {
  ast::Monitor *M4 = new ast::Monitor("M4");

  ast::State *INTERNAL__1 = new ast::State(
    {ast::Modifier::always,ast::Modifier::init},
    "INTERNAL__1",
    {},
    {
      new ast::Transition(
        new ast::Pattern(true,"command",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"c")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"n")),new ast::Constraint("kind",new ast::Range(ast::Range::Kind::VALUE,"FSW"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"Dispatch",{new ast::Constraint("dc",new ast::Range(ast::Range::Kind::NAME,"c")),new ast::Constraint("dn",new ast::Range(ast::Range::Kind::NAME,"n"))})
        }
      )
    }
  );

  ast::State *Dispatch = new ast::State(
    {ast::Modifier::hot},
    "Dispatch",
    {"dc","dn"},
    {
      new ast::Transition(
        new ast::Pattern(true,"cancel",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"dc")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"dn"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"ok",{})
        }
      )
      ,
      new ast::Transition(
        new ast::Pattern(true,"dispatch",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"dc")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"dn"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"Succeed",{new ast::Constraint("sc",new ast::Range(ast::Range::Kind::NAME,"dc")),new ast::Constraint("sn",new ast::Range(ast::Range::Kind::NAME,"dn"))})
        }
      )
    }
  );

  ast::State *Succeed = new ast::State(
    {ast::Modifier::hot},
    "Succeed",
    {"sc","sn"},
    {
      new ast::Transition(
        new ast::Pattern(true,"succeed",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"sc")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"sn"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"Close",{new ast::Constraint("cc",new ast::Range(ast::Range::Kind::NAME,"sc")),new ast::Constraint("cn",new ast::Range(ast::Range::Kind::NAME,"sn"))})
        }
      )
      ,
      new ast::Transition(
        new ast::Pattern(true,"command",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"sc")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"_")),new ast::Constraint("kind",new ast::Range(ast::Range::Kind::VALUE,"FSW"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"error",{})
        }
      )
      ,
      new ast::Transition(
        new ast::Pattern(true,"fail",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"sc")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"sn"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"error",{})
        }
      )
    }
  );

  ast::State *Close = new ast::State(
    {ast::Modifier::hot},
    "Close",
    {"cc","cn"},
    {
      new ast::Transition(
        new ast::Pattern(true,"succeed",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"cc")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"cn"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"error",{})
        }
      )
      ,
      new ast::Transition(
        new ast::Pattern(true,"close",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"cc")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"cn"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"ok",{})
        }
      )
    }
  );

  M4->addEvent("command");
  M4->addEvent("cancel");
  M4->addEvent("dispatch");
  M4->addEvent("fail");
  M4->addEvent("succeed");
  M4->addEvent("close");
  M4->addState(INTERNAL__1);
  M4->addState(Dispatch);
  M4->addState(Succeed);
  M4->addState(Close);

  return M4;
}

SpecObject makeContract() {
  ast::Spec *spec = new ast::Spec();
  spec->addMonitor(monitorM4());
  SpecObject contract(spec);
  return contract;
}
