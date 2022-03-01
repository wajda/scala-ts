// Generated by ScalaTS 0.5.10-SNAPSHOT: https://scala-ts.github.io/scala-ts/

import { Bye, isBye } from './Bye';
import { GoodBye, isGoodBye } from './GoodBye';
import { Hello, isHello } from './Hello';
import { Hi, isHi } from './Hi';
import { Whatever, isWhatever } from './Whatever';

export type Greeting = Bye | GoodBye | Hello | Hi | Whatever;

export function isGreeting(v: any): v is Greeting {
  return (
    isBye(v) ||
    isGoodBye(v) ||
    isHello(v) ||
    isHi(v) ||
    isWhatever(v)
  );
}
