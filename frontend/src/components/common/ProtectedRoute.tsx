import React from 'react';
import {Navigate} from 'react-router-dom';
import {Role} from "../../models/enums/Role.ts";
import {useAuth} from "../../context/AuthContext.tsx";

interface ProtectedRouteProps {
  children: React.ReactNode;
  requiredRole?: Role;
}

const ProtectedRoute: React.FC<ProtectedRouteProps> = ({children, requiredRole}) => {
  const {user} = useAuth();

  if (!user) {
    return <Navigate to="/signin" replace/>;
  }

  if (requiredRole && user.role !== requiredRole) {
    return <Navigate to="/dashboard" replace/>;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
