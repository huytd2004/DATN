import api from '@/services/api'

export const translateService = {
  quick({ text, sourceLang = 'ja', targetLang = 'vi' }) {
    return api.post('/api/v1/translate/quick', {
      text,
      sourceLang,
      targetLang,
    })
  },

  deep({ text, sourceLang = 'ja', targetLang = 'vi', sentenceId }) {
    return api.post('/api/v1/translate/deep', {
      text,
      sourceLang,
      targetLang,
      sentenceId,
    })
  },
}
