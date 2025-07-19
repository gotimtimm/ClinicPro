import React, { useState, useEffect, useRef } from 'react';
import { Input } from './input';
import { Button } from './button';
import { ChevronDown, X } from 'lucide-react';

interface AutocompleteProps {
  value: string;
  onChange: (value: string) => void;
  onSelect: (item: any) => void;
  placeholder: string;
  searchFunction: (query: string) => Promise<any[]>;
  displayKey: string;
  className?: string;
}

export function Autocomplete({
  value,
  onChange,
  onSelect,
  placeholder,
  searchFunction,
  displayKey,
  className = ""
}: AutocompleteProps) {
  const [isOpen, setIsOpen] = useState(false);
  const [suggestions, setSuggestions] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    const searchItems = async () => {
      if (value.length < 2) {
        setSuggestions([]);
        return;
      }

      setIsLoading(true);
      try {
        const results = await searchFunction(value);
        setSuggestions(results);
      } catch (error) {
        console.error('Search error:', error);
        setSuggestions([]);
      } finally {
        setIsLoading(false);
      }
    };

    const timeoutId = setTimeout(searchItems, 300);
    return () => clearTimeout(timeoutId);
  }, [value, searchFunction]);

  const handleSelect = (item: any) => {
    onSelect(item);
    onChange(item[displayKey]);
    setIsOpen(false);
  };

  const handleClear = () => {
    onChange('');
    onSelect(null);
    setIsOpen(false);
  };

  return (
    <div ref={wrapperRef} className={`relative ${className}`}>
      <div className="relative">
        <Input
          value={value}
          onChange={(e) => {
            onChange(e.target.value);
            setIsOpen(true);
          }}
          onFocus={() => setIsOpen(true)}
          placeholder={placeholder}
          className="pr-8"
        />
        <div className="absolute right-2 top-1/2 transform -translate-y-1/2 flex items-center gap-1">
          {value && (
            <Button
              type="button"
              variant="ghost"
              size="sm"
              onClick={handleClear}
              className="h-6 w-6 p-0"
            >
              <X className="h-3 w-3" />
            </Button>
          )}
          <ChevronDown className="h-4 w-4 text-muted-foreground" />
        </div>
      </div>

      {isOpen && (suggestions.length > 0 || isLoading) && (
        <div className="absolute z-50 w-full mt-1 bg-white border border-gray-200 rounded-md shadow-lg max-h-60 overflow-auto">
          {isLoading ? (
            <div className="px-3 py-2 text-sm text-muted-foreground">
              Searching...
            </div>
          ) : (
            suggestions.map((item, index) => (
              <button
                key={index}
                type="button"
                className="w-full px-3 py-2 text-left text-sm hover:bg-gray-100 focus:bg-gray-100 focus:outline-none"
                onClick={() => handleSelect(item)}
              >
                {item[displayKey]}
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
} 