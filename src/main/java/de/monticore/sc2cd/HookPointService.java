/* (c) https://github.com/MontiCore/monticore */
package de.monticore.sc2cd;

import de.monticore.generating.templateengine.HookPoint;
import de.monticore.generating.templateengine.StringHookPoint;
import de.monticore.generating.templateengine.TemplateHookPoint;
import de.monticore.generating.templateengine.TemplateStringHookPoint;

// TODO #3100: Die drei Methoden integrieren wir in den Tepmplatecontroller in MC selbst und machen so diese Klasse obsolet.
//  besser nocH. Klasse einfach entfernen, denn es gibt Alternativen: zum Beispiel ${glex.bindStringHookPoint("

import java.io.IOException;

@SuppressWarnings("unused")
public class HookPointService {
  public HookPoint templateHP(String template) {
    return new TemplateHookPoint(template);
  }

  public HookPoint stringHP(String value) {
    return new StringHookPoint(value);
  }

  public HookPoint templateStringHP(String statement) throws IOException {
    return new TemplateStringHookPoint(statement);
  }

}
