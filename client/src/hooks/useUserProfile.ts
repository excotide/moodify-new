import { useEffect, useState } from "react";
import { useAuthContext } from "../context/AuthContext";

const API_BASE = import.meta.env.VITE_API_URL ?? "";

type UserProfile = {
  id: string;
  username: string;
  createdAt?: string;
  [key: string]: any;
};

export const useUserProfile = () => {
  const { user } = useAuthContext();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const id = user?.uuid || localStorage.getItem("userUuid");
    if (!id) return; 

    const ac = new AbortController();
    const fetchProfile = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(`${API_BASE}/api/users/${id}`, { signal: ac.signal });
        if (!res.ok) {
          let msg = res.statusText || "Failed to fetch user";
          try {
            const errBody = await res.json();
            msg = errBody.message || errBody.error || JSON.stringify(errBody);
          } catch {}
          setError(msg);
          return;
        }
        const data = (await res.json()) as UserProfile;
        setProfile(data);
      } catch (e) {
        if ((e as any)?.name !== "AbortError") {
          setError("Network error. Please try again.");
        }
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
    return () => ac.abort();
  }, [user?.uuid]);

  return { profile, loading, error } as const;
};

export default useUserProfile;
