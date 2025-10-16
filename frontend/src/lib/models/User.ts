import {Role} from "./Role.ts";

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  role: Role;
  googleId?: string;
  pictureUrl?: string;
  createdAt: string;
  updatedAt: string;
}
