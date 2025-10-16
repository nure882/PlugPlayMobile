import React from 'react';
import {Navigate} from 'react-router-dom';
import {Role} from "../lib/models/Role.ts";
import {User} from "../lib/models/User.ts";

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: Role;
  user: User | null;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children,
                                                         requiredRole,
                                                         user
                                                       }) => {
  if (!user) {
    return <Navigate to="/login" replace/>;
  }

  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/dashboard" replace/>;
  }

  return <>{children}</>;
};

export default ProtectedRoute;