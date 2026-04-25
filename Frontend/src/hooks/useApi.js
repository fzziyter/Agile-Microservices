import { useState, useEffect, useCallback } from 'react'

/**
 * useApi(apiFn, ...args) — call on mount, returns { data, loading, error, refetch }
 */
export function useApi(apiFn, ...args) {
  const [data,    setData]    = useState(null)
  const [loading, setLoading] = useState(true)
  const [error,   setError]   = useState(null)

  const fetch = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const res = await apiFn(...args)
      setData(res.data)
    } catch (err) {
      setError(err.response?.data?.message || 'Une erreur est survenue')
    } finally {
      setLoading(false)
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [JSON.stringify(args)])

  useEffect(() => { fetch() }, [fetch])

  return { data, loading, error, refetch: fetch }
}

/**
 * useMutation(apiFn) — manual trigger, returns { mutate, loading, error }
 */
export function useMutation(apiFn) {
  const [loading, setLoading] = useState(false)
  const [error,   setError]   = useState(null)

  const mutate = useCallback(async (...args) => {
    setLoading(true); setError(null)
    try {
      const res = await apiFn(...args)
      return { ok: true, data: res.data }
    } catch (err) {
      const msg = err.response?.data?.message || 'Une erreur est survenue'
      setError(msg)
      return { ok: false, message: msg }
    } finally {
      setLoading(false)
    }
  }, [apiFn])

  return { mutate, loading, error }
}
