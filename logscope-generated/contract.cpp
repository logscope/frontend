/**********************************************/
/* C++ generated from LogScope specification! */
/**********************************************/

#include "contract.h"

using namespace std;

ast::Monitor *monitorM1() {
  ast::Monitor *M1 = new ast::Monitor("M1");

  ast::State *INTERNAL__1 = new ast::State(
    {ast::Modifier::always,ast::Modifier::init},
    "INTERNAL__1",
    {},
    {
      new ast::Transition(
        new ast::Pattern(true,"command",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"x")),new ast::Constraint("kind",new ast::Range(ast::Range::Kind::VALUE,"FSW"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"Succeed",{new ast::Constraint("c",new ast::Range(ast::Range::Kind::NAME,"x"))})
        }
      )
      ,
      new ast::Transition(
        new ast::Pattern(true,"succeed",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"x"))})
        ,
        {
          new ast::Pattern(false,"Succeed",{new ast::Constraint("c",new ast::Range(ast::Range::Kind::NAME,"x"))})
        }
        ,
        {
          new ast::Pattern(true,"error",{})
        }
      )
    }
  );

  ast::State *Succeed = new ast::State(
    {ast::Modifier::hot},
    "Succeed",
    {"c"},
    {
      new ast::Transition(
        new ast::Pattern(true,"succeed",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"c"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"ok",{})
        }
      )
      ,
      new ast::Transition(
        new ast::Pattern(true,"command",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"c"))})
        ,
        {}
        ,
        {
          new ast::Pattern(true,"error",{})
        }
      )
    }
  );

  M1->addEvent("command");
  M1->addEvent("succeed");
  M1->addState(INTERNAL__1);
  M1->addState(Succeed);

  return M1;
}

ast::Monitor *monitorM2() {
  ast::Monitor *M2 = new ast::Monitor("M2");

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
      ,
      new ast::Transition(
        new ast::Pattern(true,"succeed",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"c")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"n"))})
        ,
        {
          new ast::Pattern(false,"Succeed",{new ast::Constraint("sc",new ast::Range(ast::Range::Kind::NAME,"c")),new ast::Constraint("sn",new ast::Range(ast::Range::Kind::NAME,"n"))})
        }
        ,
        {
          new ast::Pattern(true,"error",{})
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
          new ast::Pattern(true,"NoMoreSuccess",{new ast::Constraint("nc",new ast::Range(ast::Range::Kind::NAME,"sc")),new ast::Constraint("nn",new ast::Range(ast::Range::Kind::NAME,"sn"))})
          ,
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

  ast::State *NoMoreSuccess = new ast::State(
    {},
    "NoMoreSuccess",
    {"nc","nn"},
    {
      new ast::Transition(
        new ast::Pattern(true,"succeed",{new ast::Constraint("cmd",new ast::Range(ast::Range::Kind::NAME,"nc")),new ast::Constraint("nr",new ast::Range(ast::Range::Kind::NAME,"nn"))})
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

  M2->addEvent("command");
  M2->addEvent("cancel");
  M2->addEvent("dispatch");
  M2->addEvent("fail");
  M2->addEvent("succeed");
  M2->addEvent("close");
  M2->addState(INTERNAL__1);
  M2->addState(Dispatch);
  M2->addState(Succeed);
  M2->addState(NoMoreSuccess);
  M2->addState(Close);

  return M2;
}

SpecObject makeContract() {
  ast::Spec *spec = new ast::Spec();
  spec->addMonitor(monitorM1());
  spec->addMonitor(monitorM2());
  SpecObject contract(spec);
  return contract;
}
