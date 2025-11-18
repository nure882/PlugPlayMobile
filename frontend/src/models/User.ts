import {Address} from "./Address.ts";
import {Role} from "./Role.ts";

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: Role;
  googleId?: string;
  pictureUrl?: string;
  createdAt: string;
  updatedAt: string;
  addresses: Address[];
}
